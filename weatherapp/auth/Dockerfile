# build stage
FROM golang:alpine AS builder

RUN apk add git

WORKDIR /go/src/app

# Assuming weatherapp directory is in the same directory as the Dockerfile
COPY . .

# Move to the app directory
WORKDIR /go/src/app/weatherapp/auth/src/main

# Build the Go application
RUN go build -o /go/bin/app -v .

# final stage
FROM alpine:latest

RUN apk --no-cache add ca-certificates

# Copy the binary from the builder stage
COPY --from=builder /go/bin/app /app

# Set the entrypoint
ENTRYPOINT ["/app"]

# Expose the port
EXPOSE 8080

# Metadata as defined in the original Dockerfile
LABEL Name=auth Version=0.0.1
