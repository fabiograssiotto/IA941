mkdir -p Aula1-dist/ManualController
mkdir -p Aula1-dist/ws3d
cp -r Aula1/ManualController/dist/* Aula1-dist/ManualController
cp -r Aula1/ws3d/dist/* Aula1-dist/ws3d
tar -czvf Aula1-dist.tar.gz Aula1-dist  
