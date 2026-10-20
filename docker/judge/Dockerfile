FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends g++ python3 \
    && rm -rf /var/lib/apt/lists/*

ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
COPY runner.py /opt/ses-judge/runner.py
ENTRYPOINT ["python3", "-I", "/opt/ses-judge/runner.py"]
