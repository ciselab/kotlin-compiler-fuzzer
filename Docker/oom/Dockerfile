FROM python:3.10-buster

WORKDIR /src

COPY ./requirements.txt requirements.txt

RUN pip3 install --upgrade pip

RUN pip3 install --no-cache-dir --upgrade -r requirements.txt

COPY ./app /src/app

ENV MODELDIR="/models"

EXPOSE 80

# Run the API
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "80"]
