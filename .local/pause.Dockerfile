FROM amazonlinux:latest
RUN yum install -y iptables

CMD iptables -t nat -A PREROUTING -p tcp -d 169.254.170.2 --dport 80 -j DNAT --to-destination 127.0.0.1:51679 \
 && iptables -t nat -A OUTPUT -d 169.254.170.2 -p tcp -m tcp --dport 80 -j REDIRECT --to-ports 51679 \
 && iptables-save \
 && /bin/bash -c 'while true; do sleep 30; done;'
