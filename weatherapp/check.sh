#!/bin/bash

for i  in 'weatherapp-auth' 'weatherapp-db' 'weatherapp-redis' 'weatherapp-ui' 'weatherapp-weather'
do 
RUNNING=$(docker-compose ps | grep $i | grep -o Up)

if [[ ${RUNNING} == Up ]]
  then 
  echo "$i container is in running state"
  else 
  echo "$i container is NOT in running state"
  exit 1
fi
done