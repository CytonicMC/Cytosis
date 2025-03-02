echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin
docker push ghcr.io/cytonicmc/cytosis:latest
