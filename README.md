# Happy-O-Meter Service [![Build Status](https://travis-ci.org/alphadev-sthlm/happy-o-meter-svc.svg?branch=master)](https://travis-ci.org/alphadev-sthlm/happy-o-meter-svc)

# Service startup

mvn spring-boot:run

# Example service usage

Please note that `@` must be present before `ABSOLUTE_PATH_OF_FILE`

```
curl -H "Content-Type: application/octet-stream" -v -X POST localhost:8080/emotions --data-binary @ABSOLUTE_PATH_OF_FILE -o /tmp/new-image.jpg
```

Or add this to your .bashrc/.zshrc
```
function emopost() {¬
  curl -H "Content-Type: application/octet-stream" -v -X POST localhost:8080/emotions --data-binary @$1 -o /path/to/new-image.jpg
}
```

/tmp/new-image.jpg would be the same image with a label of the strongest emotion of each face.
# happy-o-meter-svc
