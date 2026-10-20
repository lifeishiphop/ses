"""Trusted supervisor; submitted programs run under separate unprivileged UIDs."""
import json
import os
from pathlib import Path
import selectors
import signal
import subprocess
import sys
import tempfile
import time


def stop_user_processes(uid):
    # A program can create a new process group; clean up by UID as well.
    for _ in range(3):
        found = False
        for entry in Path('/proc').iterdir():
            if not entry.name.isdigit():
                continue
            try:
                status = (entry / 'status').read_text()
                actual_uid = int(next(line for line in status.splitlines() if line.startswith('Uid:')).split()[1])
                if actual_uid == uid:
                    os.kill(int(entry.name), signal.SIGKILL)
                    found = True
            except (FileNotFoundError, ProcessLookupError):
                pass
        if not found:
            return


def execute(command, stdin, directory, uid, timeout_ms, output_limit):
    output = bytearray()
    error = bytearray()
    status = 'OK'
    with tempfile.TemporaryFile() as input_file:
        input_file.write(stdin.encode('utf-8'))
        input_file.seek(0)
        process = subprocess.Popen(
            command, stdin=input_file, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            cwd=directory, user=uid, group=uid, extra_groups=[], start_new_session=True, umask=0o077,
            env={'PATH': os.environ['PATH'], 'HOME': str(directory), 'LANG': 'C.UTF-8', 'LC_ALL': 'C.UTF-8'},
        )
        try:
            deadline = time.monotonic() + timeout_ms / 1000
            with selectors.DefaultSelector() as selector:
                selector.register(process.stdout, selectors.EVENT_READ, output)
                selector.register(process.stderr, selectors.EVENT_READ, error)
                while selector.get_map() or process.poll() is None:
                    remaining = deadline - time.monotonic()
                    if remaining <= 0:
                        status = 'TIME_LIMIT'
                        break
                    for key, _ in selector.select(min(remaining, 0.05)):
                        data = os.read(key.fileobj.fileno(), 8192)
                        if not data:
                            selector.unregister(key.fileobj)
                            continue
                        available = output_limit - len(output) - len(error)
                        key.data.extend(data[:available])
                        if len(data) > available:
                            status = 'OUTPUT_LIMIT'
                            break
                    if status != 'OK':
                        break
        finally:
            stop_user_processes(uid)
            process.wait(timeout=2)
            process.stdout.close()
            process.stderr.close()
    if status == 'OK' and process.returncode != 0:
        status = 'RUNTIME_ERROR'
    return {'status': status, 'output': output.decode('utf-8', errors='replace'),
            'error': error.decode('utf-8', errors='replace')}


def judge(request):
    workspace = Path('/workspace')
    build = workspace / 'build'
    build.mkdir()
    java = request['language'] == 1
    source = build / ('Main.java' if java else 'Main.cpp')
    source.write_text(request['source'], encoding='utf-8')
    os.chown(build, 1000, 1000)
    compile_command = (['javac', '-J-XX:ActiveProcessorCount=1', '-J-XX:+UseSerialGC', '-J-Xmx256m',
                        '-encoding', 'UTF-8', '-proc:none', 'Main.java'] if java else
                       ['g++', '-std=c++17', '-O2', '-o', 'Main', 'Main.cpp'])
    compilation = execute(compile_command, '', build, 1000,
                          request['compileTimeoutMs'], request['outputLimitBytes'])
    if compilation['status'] != 'OK':
        status = 'COMPILE_ERROR' if compilation['status'] == 'RUNTIME_ERROR' else compilation['status']
        return {'status': status, 'cases': []}

    # Freeze compiled artifacts so one test cannot modify the next test's program.
    for directory, _, files in os.walk(build):
        os.chown(directory, 0, 0)
        os.chmod(directory, 0o555)
        for name in files:
            artifact = Path(directory) / name
            os.chown(artifact, 0, 0)
            os.chmod(artifact, 0o555 if name == 'Main' else 0o444)

    command = (['java', '-XX:ActiveProcessorCount=1', '-XX:+UseSerialGC', '-Xms16m', '-Xmx128m',
                '-XX:MaxMetaspaceSize=96m', '-XX:ReservedCodeCacheSize=32m',
                '-Dfile.encoding=UTF-8', '-cp', str(build), 'Main'] if java else [str(build / 'Main')])
    results = []
    for index, stdin in enumerate(request['inputs']):
        uid = 10000 + index
        directory = workspace / ('case-' + str(index))
        directory.mkdir(mode=0o711)
        os.chown(directory, uid, uid)
        result = execute(command, stdin, directory, uid, request['runTimeoutMs'], request['outputLimitBytes'])
        results.append(result)
        if result['status'] != 'OK':
            break
    return {'status': 'OK', 'cases': results}


if __name__ == '__main__':
    try:
        result = judge(json.load(sys.stdin))
        print(json.dumps(result, ensure_ascii=False), flush=True)
    except Exception as error:
        print(type(error).__name__ + ': ' + str(error), file=sys.stderr, flush=True)
        sys.exit(1)
