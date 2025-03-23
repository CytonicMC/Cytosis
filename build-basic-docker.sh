docker stop cytosis_lobby || true
docker rm cytosis_lobby || true
docker build -t cytosis --build-arg OTEL_PORT="$OTEL_PORT" --build-arg OTEL_HOST="$OTEL_HOST" --no-cache --progress plain -f docker/basic/Dockerfile .
docker run --network host --name cytosis_lobby -d cytosis:latest
#docker push ghcr.io/cytonicmc/cytosis:latest
