FROM rust:buster

WORKDIR /src

COPY ./requirements.txt /code/requirements.txt

RUN apt-get update

RUN apt-get install -y python3 python3-pip

# Update pip
RUN pip3 install --upgrade pip

# Install requirements
RUN pip3 install --no-cache-dir --upgrade -r /code/requirements.txt

COPY ./app /src/app

# Run the API
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "80"]