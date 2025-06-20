docker stop cytosis_lobby || true
docker rm cytosis_lobby || true
docker buildx build -t cytosis --platform linux/amd64 --build-arg OTEL_PORT="4317" --build-arg OTEL_HOST="homelab" --load --progress plain -f docker/basic/Dockerfile .
docker run --network host --name cytosis_lobby -d cytosis:latest