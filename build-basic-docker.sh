docker stop cytosis_lobby || true
docker rm cytosis_lobby || true
docker buildx build -t cytosis --platform linux/amd64 --build-arg OTEL_PORT="$OTEL_PORT" --build-arg OTEL_HOST="$OTEL_HOST" --load --progress plain -f docker/basic/Dockerfile .
docker run --network host --name cytosis_lobby -d cytosis:latest