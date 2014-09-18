#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
${symbol_pound}!/usr/bin/python

from ${service_lowercase}.client import ${service_titlecase}Client

def main():
    outgoing = 'HELLO WORLD!'
    client = ${service_titlecase}Client()
    print 'sending to server: {0}'.format(outgoing)
    incoming = client.basicRequest(outgoing)
    print 'server sent back: {0}'.format(incoming.response)


if __name__=='__main__':
    main()
