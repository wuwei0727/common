docker-compose down
sleep 10s
docker-compose up -d
sleep 20s
cd /home/jar/newcoal/
nohup java -jar location-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod &
sleep 20s
nohup java -jar check-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod &
sleep 20s
nohup java -jar area_warn-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod &
sleep 20s
nohup java -jar web-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod & 
echo "start end"


