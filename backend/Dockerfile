FROM python:3.12-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY learner_api ./learner_api
COPY pyproject.toml .
COPY entrypoint.sh /app/entrypoint.sh

RUN mkdir -p /app/data /app/secrets && chmod +x /app/entrypoint.sh

EXPOSE 8080

CMD ["/app/entrypoint.sh"]
