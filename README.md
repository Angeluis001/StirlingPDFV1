# Stirling-PDF (Made in 1 day with 100% ChatGPT, Even this readme!)

This is a locally hosted web application that allows you to perform various operations on PDF files, such as splitting and adding images.

I will support and fix/add things to this if there is a demand [Discord](https://discord.gg/Cn8pWhQRxZ)


![stirling-home](images/stirling-home.png)


## Features

- Split PDFs into multiple files at specified page numbers or extract all pages as individual files.
- Merge multiple PDFs together into a single resultant file
- Convert PDFs to and from images
- Reorganize PDF pages into different orders.
- Add images to PDFs at specified locations.
- Dark mode support.

## Technologies used
- Spring Boot + Thymeleaf
- PDFBox
- HTML, CSS, JavaScript
- Docker

## How to use

### Locally 

Prerequisites
- Java 17 or later
- Gradle 6.0 or later

1. Clone or download the repository.
2. Build the project using Gradle by running `./gradlew build`
3. Start the application by running `./gradlew bootRun`


### Docker
https://hub.docker.com/r/frooodle/s-pdf

docker pull frooodle/s-pdf

docker run -p 8080:8080 frooodle/s-pdf


## How to View
1. Open a web browser and navigate to `http://localhost:8080/`
2. Use the application by following the instructions on the website.

## Note
The application is currently not thread-safe
