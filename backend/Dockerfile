FROM python:3.12-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY learner_api ./learner_api
COPY pyproject.toml .

RUN mkdir -p /app/data

EXPOSE 8080

CMD ["uvicorn", "learner_api.main:app", "--host", "0.0.0.0", "--port", "8080"]
