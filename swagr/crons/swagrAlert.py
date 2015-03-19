#!/usr/bin/python

import sys

import datetime
from operator import itemgetter
import logging
import MySQLdb
import smtplib
import email
import email.encoders
import email.mime.text
import email.mime.base

SENDMAIL_LOC = "/usr/sbin/sendmail"
RECIPIENT = "swagr-alert@rhythm.com"


def startScan():
    """
        Desc: interface function for retrospect log crawler, also initializes 
              dictionaries for requests and hosts. 
    """
    DBHOST = 'localhost'
    DB = 'retrospect'

    try:
        logging.basicConfig(
                            level=logging.DEBUG,
                            format='%(asctime)s %(levelname)s %(message)s', 
                            datefmt='%m/%d/%Y %H:%M:%S'
                            )

        con = MySQLdb.connect(
                            host = DBHOST, 
                            user = 'root',
                            db = DB
                            )

        cur = con.cursor()
        
        today = datetime.datetime.today()
        dayOfWeek = today.weekday()
        yesterday = today - datetime.timedelta(days=1)

        if dayOfWeek != 6 and dayOfWeek != 7:
            compMvgAvg(cur,yesterday)
        
        
    except MySQLdb.Error, e:
        logging.error("Error %d: %s" % (e.args[0], e.args[1]))
        sys.exit(1)
        
    finally:               
        if con:    
            con.close()


def compMvgAvg(cursor,yesterday):
    rids = []
    twoDaysAgo = yesterday - datetime.timedelta(days=1)
    try:
        cursor.execute(
            "SELECT s.rid,r.service,r.function,r.args,s.count,\
            m.value,s.location,d.name,st.count FROM \
            data_type d, requests r, moving_avgs m, stats_daily s \
            LEFT JOIN stats_daily st ON st.rid=s.rid AND \
            st.location=s.location AND st.data_type=s.data_type WHERE \
            s.data_type = d.id AND r.id=s.rid AND m.rid=s.rid \
            AND m.data_type=s.data_type AND s.location=m.location \
            AND s.logdate = %s AND s.count >20000 AND \
            m.dt = %s AND s.count>=(m.value*3) AND m.type = 1 \
            AND st.logdate = %s AND s.rid IN (SELECT rid FROM stats_daily \
            GROUP BY rid HAVING COUNT(rid) > 30)",
            (
                yesterday.strftime('%Y-%m-%d'),
                twoDaysAgo.strftime('%Y-%m-%d'),
                twoDaysAgo.strftime('%Y-%m-%d')
            )
        )
        res = cursor.fetchall()
        for row in res:
            r = {}
            r["rid"] = row[0]
            r["service"] = row[1]
            r["function"] = row[2]
            r["args"] = row[3]
            r["statsCount"] = row[4]
            r["avgCount"] = row[5]
            r["location"] = row[6]
            r["dataType"] = row[7]
            r["yesterdayCount"] = row[8]
            rids.append(r)
        
    except MySQLdb.Error, e:
        logging.error("Failed to retrieve host or request information from "
                      "database, exiting program now")
        sys.exit(1)

    if len(rids) != 0:
        ridSorted = sorted(rids, key=itemgetter("statsCount"), reverse=True)
        msg = ""
        for rid in ridSorted:
            yesterdayIncrease = percentShift(rid["yesterdayCount"],rid["statsCount"])
            avgIncrease = percentShift(rid["avgCount"],rid["statsCount"])
            msg += str(rid["service"]) + ":" + str(rid["function"]) + "(" + str(rid["args"]) + ") db ID: " + str(rid["rid"]) +"\n\t"
            msg += str(thousands(rid["statsCount"])) + " : Yesterday's request volume.\n\t"
            msg += str(thousands(rid["avgCount"])) + " : 30-day moving average as of two days ago.\n\t"
            msg += yesterdayIncrease + " change from the prior days count volume of "+ str(thousands(rid["yesterdayCount"]))+"\n\t"
            msg += avgIncrease + " change from the prior days moving average.\n\t"
            msg += "In location: " + rid["location"] + ", using language: " + rid["dataType"]+"\n\n"

        emailSpike(msg,yesterday)


def percentShift(original, new):
    increase = int(float(new-original)/float(original)*100)
    if increase < 0:
        decrease = int(float(original-new)/float(original)*100)
        return "-" + str(decrease) + "%"
    else: return "+" + str(increase) + "%"


def thousands(x):
    if type(x) is not int and type(x) is not long:
        raise TypeError("Not an integer!")
    if x < 0:
        return '-' + thousands(-x)
    elif x < 1000:
        return str(x)
    else:
        return thousands(x / 1000) + ',' + '%03d' % (x % 1000)
    

def emailSpike(message,yesterday):
    mailFrom = "swagrAlert@pranamail.com"
    mailSubject = "Subject: SWAGr Alert: LoUIE usage spike for "+ yesterday.strftime('%m/%d/%y') +"\n"
    msg = "There appears to have been a spike in usage by one or more of the LoUIE service functions.\n\n"
    msg += "To view the associated graphs, please go to: http://louiehost/swagr\n\n\n"
    msg += message
    sendMail(mailFrom, RECIPIENT, mailSubject, msg)


def sendMail(mailFrom, mailTo, mailSubject, body, attachment=None):
    msg = email.MIMEMultipart.MIMEMultipart('alternative')
    msg['Subject'] = mailSubject
    msg['From'] = mailFrom
    msg['To'] = mailTo
    msg.attach(email.mime.text.MIMEText(body, 'html'))

    if attachment:
        fileMsg = email.mime.base.MIMEBase('application', 'vnd.mozilla')
        fileMsg.set_payload(file(attachment).read())
        email.encoders.encode_base64(fileMsg)
        fileMsg.add_header('Content-Disposition', 'attachment;filename=%s' % attachment)
        msg.attach(fileMsg)

    server = smtplib.SMTP('mail')
    server.sendmail(mailFrom, mailTo, msg.as_string())
    server.quit()


if __name__ == '__main__':
    startScan()
