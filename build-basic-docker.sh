docker build -t cytosis --no-cache --progress plain -f docker/basic/Dockerfile .
docker run --network host --name cytosis_lobby -d cytosis:latest
#docker push ghcr.io/cytonicmc/cytosis:latest
