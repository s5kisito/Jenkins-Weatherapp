#!/bin/bash
ip_address=$(curl -s https://api.ipify.org ; echo)
ip_port=3000
curl http://$ip_address:$ip_port/login | grep '<title>Weather app login</title>'

if 
  [[  $? -ne 0 ]] 
then 
echo "The website is not comming up, please check"
exit 1
fi