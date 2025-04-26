docker buildx build --push \
  -t ghcr.io/cytonicmc/cytosis:latest \
  --platform linux/arm64/v8,linux/amd64 \
  --build-arg OTEL_PORT="$OTEL_PORT" \
  --build-arg OTEL_HOST="$OTEL_HOST" \
  --progress plain -f docker/basic/Dockerfile .