#!/usr/bin/python
import sys

import argparse
import datetime
import logging
import MySQLdb
import os
import re 
import subprocess
import shlex

import swagrAlert #local file swagrAlert.py triggers check for spikes in usage MUST BE CONFIGURED WITH EMAIL ADDRESSES

import urllib2


class ReqData():
    """Basic data object to capture and maintain a data point

    This class (object) was built specifically to collect and collapse data from
    louie logs for a single hour, though it could be used for any amount
    of time. After initialization, the object can be expanded via the addRows
    method until you're done adding and then the final calculations are done in
    the completeData method, which then returns the collapsed set of data.
    """
    def __init__(self, rid, loc, date, time, succ, dataType, usrAgnt, accTime,  
                    size, rows, service, func, params, clientTime=0):
        self._rid = rid
        self._date = date
        self._time = time
        self._dataType = dataType
        self._usrAgnt = usrAgnt
        self._service = service
        self._function = func
        self._params = params
        self._loc = loc
        self._accTime = int(accTime)
        self._clientTime = int(clientTime)
        self._size = int(size)
        self._rows = int(rows)
        self._minT = int(accTime)
        self._maxT = int(accTime)
        self._minCT = int(clientTime)
        self._maxCT = int(clientTime)
        self._maxBytes = int(size)
        self._maxRows = int(rows)

        #self._type = None
        self._aveCT = 0 #average client time
        self._aveT = 0 #average server time
        self._aveBytes = 0
        self._aveRows = 0
        self._fails = 0
        self._count = 1
        if succ=="ERROR":
            self._fails = 1    

    def addRows(self, succ, accTime, size, rows, clientTime=0):
        self._count += 1
        if succ=="ERROR":
            self._fails += 1
        if int(accTime)<self._minT:
            self._minT = int(accTime)
        elif int(accTime)>self._maxT:
            self._maxT = int(accTime)
        if int(clientTime)<self._minCT:
            self._minCT = int(clientTime)
        elif int(clientTime)>self._maxCT:
            self._maxCT = int(clientTime)    
        if int(size)>self._maxBytes:
            self._maxBytes = int(size)
        if int(rows)>self._maxRows:
            self._maxRows = int(rows)
        self._accTime += int(accTime)
        self._clientTime += int(clientTime)
        self._size += int(size)
        self._rows += int(rows)
        
    def completeData(self):
        self._aveT = self._accTime/self._count
        self._aveCT = self._clientTime/self._count
        self._aveBytes = self._size/self._count
        self._aveRows = self._rows/self._count
        return [self._rid, self._date, self._time, self._count, self._minT, 
                self._aveT, self._maxT, self._aveBytes, self._maxBytes,
                self._aveRows, self._maxRows, self._fails, self._loc, self._dataType, 
                self._minCT, self._aveCT, self._maxCT ]

#------------------------------------------------------------------------------
#------------------------------------------------------------------------------
#------------------------------------------------------------------------------
#------------------------------------------------------------------------------


def main():
    """
        Desc: interface function for retrospect log crawler, also initializes 
              dictionaries for requests and hosts. 
    """
    DBHOST = 'localhost'
    DB = 'retrospect'
    TMPDIR = '/usr/scratch/retrospect'
    TMPLOG = TMPDIR + '/tmpReqLog'
    
    if not os.path.exists(TMPDIR):
        ret = subprocess.Popen(["/bin/mkdir", TMPDIR], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        
        
        x = ret.communicate()
        if x[1]:
            logging.error("Failed to create " + TMPDIR + ", mkdir returned: " + x[1])
            sys.exit(-1)

    parser = argparse.ArgumentParser(description = "This script culls data"
    " from log files and populates a database with log summary data.")
    parser.add_argument('-s','--scan', help = 'Start primary crawler function, \
    scan logs, collapse/sum data, add to database', action = 'store_true')
    parser.add_argument('-m', '--manual', help = 'Manual Mode, \
    This will prompt the user to choose from a variety of modes. \
    This is an initialization function and will not \
    run for logs which have already been scanned.', action = 'store_true')
    parser.add_argument('-t', '--top', help = 'Manual Top X mode, \
    This will compute/recompute each Top X variant for a range of \
    days.', action = 'store_true')
    args = parser.parse_args() 
 
    if (args.scan):
        hostD = {}
        requestD = {}
        serviceD = {}
        dayList = []
        reqCnt = 0
        con = None
        try:
            logging.basicConfig(level=logging.DEBUG,
                                format='%(asctime)s %(levelname)s %(message)s', 
                                datefmt='%m/%d/%Y %H:%M:%S')

            con = MySQLdb.connect(host = DBHOST, user = 'root', db = DB)

            cur = con.cursor()
            
            defaultDay = datetime.datetime.today() - datetime.timedelta(days=1)
            today = datetime.datetime.today()
            dayList.append(defaultDay)       
            init(cur, hostD, requestD, serviceD)
            
            for h in hostD:
                cnt = scan(hostD[h], requestD, TMPLOG, cur, defaultDay)
                con.commit()
                reqCnt += cnt
            logging.info("Finished all hosts.")

            missedDays = []
            for num in range(2,5):
                missedDays.append(today - datetime.timedelta(days=num))
                dayList.append(today - datetime.timedelta(days=num))

            reqCnt += checkForMissed(con, missedDays, hostD, requestD, TMPLOG)                          
    
            compressToDays(cur,dayList)
            con.commit()

            topX(cur, dayList, hostD)
            con.commit()
            
            topXLoad(cur, dayList, hostD)
            con.commit()
            
            topXDuration(cur, dayList, hostD)
            con.commit()
            
            movingAverage(cur, dayList)
            con.commit()
            
            logging.info("DONE. Total Queries Scanned: " + str(reqCnt))
            
            #swagrAlert.startScan() #NEEDS TO BE CONFIGURED WITH EMAIL ADDRESSES FIRST
            
        except MySQLdb.Error, e:
            logging.error("Error %d: %s" % (e.args[0], e.args[1]))
            sys.exit(1)
            
        finally:               
            if con:    
                con.close()


    if (args.manual):
        hostD = {}
        requestD = {}
        serviceD = {}
        dayList = []
        reqCnt = 0
        con = None

        var = raw_input("Please select from one of the following options:\n"
        "S -- single mode: run scan, top20 and day compression for a "
        "given day\n"
        "R -- range mode: run scan, top20 and day compression for a range of "
        "days.\nNote that if a log has already been scanned it will not "
        "be re-scanned\n")
        s = re.compile(r'S', re.I)
        r = re.compile(r'R', re.I)
        single = s.search(var)
        ranged = r.search(var)
        
        if single:
            day = raw_input("Please enter day to be scanned as <MM-DD-YY>\n")
            target = datetime.datetime.strptime(day, "%m-%d-%y")
            dayList.append(target)
        elif ranged:
            start = raw_input("Please enter start day as <MM-DD-YY>\n")
            startDay = datetime.datetime.strptime(start, "%m-%d-%y")
            end = raw_input("Please enter end day as <MM-DD-YY>\n")
            endDay = datetime.datetime.strptime(end, "%m-%d-%y")
            dayRange = (endDay+datetime.timedelta(days=1)-startDay).days
            for i in range(dayRange):
                dayList.append(startDay+datetime.timedelta(days=i))
        else:
            print "Invalid selection, exiting"
            sys.exit(0)

        try:
            logging.basicConfig(filename='/src/mis/retrospect/retrospect.log',
                                level=logging.DEBUG,
                                format='%(asctime)s %(levelname)s %(message)s', 
                                datefmt='%m/%d/%Y %H:%M:%S')

            con = MySQLdb.connect(host = DBHOST, user = 'root', db = DB)

            cur = con.cursor()
            logging.info("Manual date mode")
            init(cur, hostD, requestD, serviceD)
            
            for day in dayList:
                for h in hostD:
                    cnt = scan(hostD[h], requestD, TMPLOG, cur, day)
                    con.commit()
                    reqCnt += cnt
            logging.info("Finished all hosts.")

            compressToDays(cur,dayList)
            con.commit()

            topX(cur, dayList, hostD)
            con.commit()       

            topXLoad(cur, dayList, hostD)
            con.commit()
            
            topXDuration(cur, dayList, hostD)
            con.commit()
            
            movingAverage(cur, dayList)
            con.commit()
            
            logging.info("DONE. Total Queries: " + str(reqCnt))

        except MySQLdb.Error, e:
            logging.error("Error %d: %s" % (e.args[0], e.args[1]))
            sys.exit(1)
            
        finally:               
            if con:    
                con.close()
        
    if(args.top):
        hostD = {}
        requestD = {}
        dayList = []
        #reqCnt = 0
        con = None

        start = raw_input("Please enter start day as <MM-DD-YY>\n")
        startDay = datetime.datetime.strptime(start, "%m-%d-%y")
        end = raw_input("Please enter end day as <MM-DD-YY>\n")
        endDay = datetime.datetime.strptime(end, "%m-%d-%y")
        dayRange = (endDay+datetime.timedelta(days=1)-startDay).days
        for i in range(dayRange):
            dayList.append(startDay+datetime.timedelta(days=i))
        
        try:
            logging.basicConfig(filename='/src/mis/retrospect/retrospect.log',
                                level=logging.DEBUG,
                                format='%(asctime)s %(levelname)s %(message)s', 
                                datefmt='%m/%d/%Y %H:%M:%S')

            con = MySQLdb.connect(host = DBHOST, 
                                user = 'root', 
                                db = DB)

            cur = con.cursor()
            logging.info("Manual TOP X mode")
            init(cur, hostD, requestD)
            
            topX(cur, dayList, hostD)
            con.commit()       

            topXLoad(cur, dayList, hostD)
            con.commit()
            
            topXDuration(cur, dayList, hostD)
            con.commit()
            
        except MySQLdb.Error, e:
            logging.error("Error %d: %s" % (e.args[0], e.args[1]))
            sys.exit(1)
            
        finally:               
            if con:    
                con.close()
            
def scan(host, requestD, tmplog, cursor, targDay):
    """
        Args: (dict): host, specific host to scan, contains host info
              (dict): requestD, all known request types, dict of dict
              (string): tmplog, temp log dir+filename
              (mysql obj): cursor, mysql cursor object
              (datetime): targDay, the day to scan
     Returns: (int): cnt, the total number of queries scanned
        Desc: The primary scan function, checks for residual copied log files,
              transfers requested log file from host, 
              initializes relevant scan type by system, 
              logs host scan, cleans up.
    """
    cnt = 0
    if os.path.isfile(tmplog):
        try:
            ret = subprocess.Popen([
                                        "/bin/rm", 
                                        tmplog,
                                    ], stdout=subprocess.PIPE).communicate()[0]
        except(OSError,ValueError):
            logging.error("Failed to remove residual logfile, breaking from"
                          " program because this must be resolved")
            sys.exit(1)

    filepattern = host["filepattern"]
    filedir = host["logdir"]
    hostnm = host["h_name"]
    hostloc = host["location"]
    systemid = host["systemid"]
    hostid = host["hostid"]
    web = host["web"]
    
    targFile = targDay.strftime(filepattern)

    cursor.execute("SELECT id, hostid, filename FROM host_scans WHERE \
    filename=%s and hostid=%s", (targFile,hostid))
    res = cursor.fetchall()
    if res != ():
        logging.info("Log file: " + targFile + " on hostid# " 
                     "" + str(hostid) + " has "
                     "already been scanned. Skipping...")
    else:
        if web:
            trans = transferFileHttp(filedir, targFile, tmplog)
        else:
            trans = transferFile(hostnm, "{0}/{1}".format(filedir,targFile), tmplog)

        if trans:
            startscn = None
            endscn = None
            
            if host["name"] == "louie":
                startscn = datetime.datetime.now()
                cnt = louieScan(cursor, tmplog, systemid, hostloc, requestD)
                endscn = datetime.datetime.now()
            #mark it in the host_scans table
            cursor.execute("INSERT INTO host_scans (hostid, filename, start, "
                           "end, count) VALUES (%s, %s, %s, %s, %s)",
                (
                hostid,
                targFile,
                startscn.strftime('%Y-%m-%d %H:%M:%S'),
                endscn.strftime('%Y-%m-%d %H:%M:%S'),
                cnt
                )
            )
        else:
            logging.warning("File was not successfully transferred, "
                            "skipping...")
        #delete copy
        if os.path.isfile(tmplog):
            try:
                ret = subprocess.Popen([
                                    "/bin/rm", 
                                    tmplog,  
                                    ],
                                    stdout=subprocess.PIPE).communicate()[0]
            except(OSError,ValueError):
                logging.error("Failed to remove residual logfile, breaking from"
                              " program because this must be resolved")
                sys.exit(-1)

    return cnt


def louieScan(cursor, log, systemid, loc, requestD):
    """
        Args: (mysql obj): cursor, mysql cursor object
              (string): log, log file to attempt to open
              (string): systemid, the system id number
              (string): loc, the location that this log file originated in 
              (dict): requestD, all known request types, dict of dict
     Returns: (int): ttlCnt, the total number of queries scanned
        Desc: A louie specific scan function, the regex is tailored to louie log
              structure. As data is collected it is added to the multi level 
              dict object "days" via the buildMultiDict function. Upon
              completion, the collected data is then inserted into the 
              stats_hourly table
    """
    logging.info("Scanning...")    
    ttlCnt = 0
    days = {}
    failsCnt = 0
    lg = re.compile(r"""\[([\w.-]+)\s+([\w:.-]+)\|(.*?)\|(.*?)\|(.*?)\|(.*?)\|#
                    (.*?)\|(.*?)\|(.*?)\|(.*?)\]\s*(\w+):(\w+)\((.*?)\)""",re.X)
    py = re.compile(r'python', re.I)
    objc = re.compile(r'ivy', re.I)
    jva = re.compile(r'java', re.I)
    prl = re.compile(r'perl', re.I)
    lou = re.compile(r'LoUIE', re.I)
    dtype = 1 #unknown

    f = open(log,'r')    
    for line in f:
        match = lg.search(line)       

        if match:
            ttlCnt += 1

            dt = match.group(1)         #date
            time = match.group(2)       #time
            success = match.group(3)    #success/fail
            address = match.group(4)    #address
            user = match.group(5)       #user
            dataType = match.group(6)   #data type
            usrAgnt = match.group(7)    #user agent
            accessTimes = match.group(8)   #time in ms
            accessRes = re.search(r'([\d]+)/([\d]+)ms',accessTimes) #strip off /##ms
            if accessRes is None:
                logging.error("Failed to parse times for field: " + accessTimes)
                continue
            accessTm = accessRes.group(1)
            clientTm = accessRes.group(2)
            size = match.group(9)       #size in bytes
            size = size.strip('b')
            rows = match.group(10)      #number of rows
            service = match.group(11)   #service
            function = match.group(12)  #function
            params = match.group(13)    #params list
            
            if dataType=="JSON":
                dtype = 2#'json'
            else:
                python = py.search(usrAgnt)
                python2 = py.search(dataType)
                objectivec = objc.search(usrAgnt)
                objectivec2 = objc.search(dataType)
                java = jva.search(usrAgnt)
                java2 = jva.search(dataType)
                perl = prl.search(usrAgnt)
                perl2 = prl.search(dataType)
                louie = lou.search(usrAgnt)
                louie2 = lou.search(dataType)
                if python:
                    dtype = 3#'python'
                elif python2:
                    dtype = 3#'python'
                elif objectivec:
                    dtype = 4#'objective c'
                elif objectivec2:
                    dtype = 4#'objective c'
                elif java:
                    dtype = 5#'java'
                elif java2:
                    dtype = 5#'java'
                elif perl:
                    dtype = 6#'perl'
                elif perl2:
                    dtype = 6#'perl'
                elif louie:
                    dtype = 8#'louie'
                elif louie2:
                    dtype = 8#'louie'
                else:
                    dtype = 1#'unknown'

            req = service + ':' + function + '(' + params + ')' + str(systemid)
            reqDType = req + '_' + str(dtype)

            rid = None
            if req not in requestD:
                if requestD:
                    rid = int(max(requestD.itervalues()))+1
                    insertReq(cursor, rid, service, function, params, systemid)
                    requestD[req] = rid
                else:
                    rid = 1
                    insertReq(cursor, rid, service, function, params, systemid)
                    requestD[req] = rid
            else:
                rid = requestD[req]

            hour = re.search(r'\d\d?',time)

            if hour:
                hr = hour.group() + ':00:00'
                buildMultiDict(days, reqDType, rid, loc, dt, hr, success, dtype,
                                usrAgnt, accessTm, size, rows, service,
                                function, params, clientTm)

    f.close()    
    dbInsert(cursor, days)

    return ttlCnt


def buildMultiDict(days, req, rid, loc, dt, hr, success, dataType, usrAgnt,
                    accessTm, size, rows, service, function, params, clientTm=0):
    """
        Args: (dict): days, a ref to the days dict which holds all the collapsed
              data points. 
              (string(s)): all of the collected strings from the originating 
              scan function.
        Desc: An evaluative function which stores data into the days dict 
              according to day,hour and reqid
    """
    if dt in days:
        if hr in days[dt]:
            if req in days[dt][hr]:
                days[dt][hr][req].addRows(success, accessTm, size, rows, clientTm) 
            else:
                hourObj = ReqData(rid, loc, dt, hr, success, dataType, usrAgnt,
                                    accessTm, size, rows, service, function,
                                    params, clientTm)
                days[dt][hr][req] = hourObj
        else:
            hour = {}
            hourObj = ReqData(rid, loc, dt, hr, success, dataType, usrAgnt,
                                accessTm, size, rows, service, function, params, clientTm)                          
            hour[req] = hourObj
            days[dt][hr] = hour
    else:
        date = {}
        hour = {}
        hourObj = ReqData(rid, loc, dt, hr, success, dataType, usrAgnt, accessTm,
                            size, rows, service, function, params, clientTm)
        hour[req] = hourObj
        date[hr] = hour
        days[dt] = date  


def init(cursor, hostD, requestD, serviceD):
    """
        Args: (mysql obj): cursor, mysql cursor object
              (dict): hostD, collection of hosts and info, dict of dict
              (dict): requestD, all known request types, dict of dict
        Desc: An evaluative function which stores data into the days dict 
              according to day,hour and reqid
    """
    logging.info("Fetching host and "
                 "request information")
    try:
        cursor.execute("SELECT h.id, s.name, h.systemid, h.name, h.logdir, \
        h.filepattern, h.location, h.web FROM host h, system s \
        WHERE h.systemid = s.id AND h.active")
        res = cursor.fetchall()
        for row in res:
            h = {}
            h["hostid"] = row[0]
            h["name"] = row[1]
            h["systemid"] = row[2]
            h["h_name"] = row[3]
            h["logdir"] = row[4]
            h["filepattern"] = row[5]
            h["location"] = row[6]
            h["web"] = row[7]
            hostD[row[0]] = h
        
        cursor.execute("SELECT id,service,function,args,systemid from \
        requests")
        res = cursor.fetchall()
        for row in res:
            req = str(row[1])+":"+str(row[2])+"("+str(row[3])+")"+str(row[4])
            requestD[req]=row[0]
            
        #cursor.execute("SELECT DISTINCT service,systemid FROM requests")
        #res = cursor.fetchall()
        #for row in res:
        #    if row[1] in serviceD:
        #        serviceD[row[1]].append(row[0])
        #    else:
        #        s = []
        #        serviceD[row[1]] = s.append(row[0])
            
        return 1
    except MySQLdb.Error, e:
        logging.error("Failed to retrieve host or request information from "
                      "database, exiting program now")
        sys.exit(1)


def insertReq(cursor, rid, serv, func, params, systype):
    """
        Args: (mysql obj): cursor, mysql cursor object
              (string(s)): list of strings to be added as new request
        Desc: Inserts newly found requests according to what is/isn't in 
              requests dictionary. 
    """
    logging.info("New RID found: " + str(rid) + ". Adding now...")
    try:
        cursor.execute("INSERT INTO requests (id, service, function, args, \
        systemid) VALUES (%s, %s, %s, %s, %s)", 
            (rid, serv, func, params, systype)
        )    
    except MySQLdb.Error, e:
        logging.error("Failed to insert new REQ ID into requests table, "
                      "\n\t\t\tRequest components were: \
        " + serv + ":" + func + "(" + params + ")")
        sys.exit(1)


#almost completely illegible in terms of simplicity
def dbInsert(cursor, days):
    """
        Args: (mysql obj): cursor, mysql cursor object
              (dict): days, the complete data dictionary for a single log
        Desc: A basic storing function, with the small caveat that if it finds a
              prior entry (for edge cases where data was already in a spot for 
              part of an hour and now we're trying to add the rest of that hour)
              it will re-evaluate using new and old data in order to update the
              row.
    """
    logging.info("Storing...")
    for day in days:                
        for hour in days[day]:
            for req in days[day][hour]:
                objList = days[day][hour][req].completeData()
            
                try:
                    cursor.execute("INSERT INTO stats_hourly (rid, logdate, \
                    loghour, count, min_time, ave_time, max_time, ave_bytes, \
                    max_bytes, ave_rows, max_rows, fails, location, data_type, min_client_time, ave_client_time, max_client_time) \
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)\
                    ON DUPLICATE KEY UPDATE \
                    count = %s + count,\
                    min_time = LEAST(%s, min_time),\
                    ave_time = ((ave_time*count)+(%s*%s))/(%s+count),\
                    max_time = GREATEST(%s,max_time),\
                    min_client_time = LEAST(%s, min_client_time),\
                    ave_client_time = ((ave_client_time*count)+(%s*%s))/(%s+count),\
                    max_client_time = GREATEST(%s,max_client_time),\
                    ave_bytes = ((ave_bytes*count)+(%s*%s))/(%s+count),\
                    max_bytes = GREATEST(%s,max_bytes),\
                    ave_rows = ((ave_rows*count)+(%s*%s))/(%s+count),\
                    max_rows = GREATEST(%s,max_rows),\
                    fails = %s+fails",
                        ( 
                        objList[0], #rid
                        objList[1], #date
                        objList[2], #hour
                        objList[3], #count
                        objList[4], #min_t
                        objList[5], #ave_t
                        objList[6], #max_t
                        objList[7], #ave_bytes
                        objList[8], #max_bytes
                        objList[9], #ave_rows
                        objList[10],#max_rows
                        objList[11],#fails
                        objList[12],#location
                        objList[13], #data_type
                        objList[14], #min_client_t
                        objList[15], #ave_client_t
                        objList[16], #max_client_t

                        objList[3], #count
                        objList[4], #min_t
                        objList[5], #ave_t
                        objList[3], #count
                        objList[3], #count
                        objList[6], #max_t
                        objList[14], #min_client_t
                        objList[15], #ave_client_t
                        objList[3], #count
                        objList[3], #count
                        objList[16], #max_client_t
                        objList[7], #ave_bytes
                        objList[3], #count
                        objList[3], #count
                        objList[8], #max_bytes
                        objList[9], #ave_rows
                        objList[3], #count
                        objList[3], #count
                        objList[10],#max_rows
                        objList[11],#fails
                        )     
                    )
              

                except MySQLdb.Error, e:
                    logging.error("Error inserting row into stats_hourly for "
                                  "rid: " + str(objList[0]) + " at date/time:"
                                  " " + str(objList[1]) + "/" + str(objList[2]))
                    sys.exit(1)
    return 1                   


def checkForMissed(con, daysRange, hostD, requestD, tmplog):
    """
        Args: (mysql obj): con, mysql connection (direct connection)
              (list): daysRange, list of days to check across
              (dict): hostD, collection of hosts and info, dict of dict
              (dict): requestD, all known request types, dict of dict
              (string): tmplog, temp log dir+filename
     Returns: (int): ttlCnt, the total number of queries scanned
        Desc: Function to check contents of host_scans table to identify log 
              files which were previously missed.
    """
    cur = con.cursor()
    ttlCnt = 0
    logging.info("Checking for logs going back"
                 " an extra " + str(len(daysRange)) + " days.")
    for host in hostD:
        filepattern = hostD[host]["filepattern"]
        hostid = hostD[host]["hostid"]                  
        for day in daysRange:
            targFile = day.strftime(filepattern)
            cur.execute("SELECT * FROM host_scans WHERE filename=%s AND "
                        "hostid=%s", 
                           (
                                targFile,
                                hostid
                           ) 
                       )
            res = cur.fetchall()
            if res == ():
                logging.info("Absent scan from system "
                             "" + hostD[host]["name"] + " "
                             "on host " + hostD[host]["h_name"] + " located, "
                             "processing now.")
                ttlCnt += scan(hostD[host], requestD, tmplog, cur, day)      
                con.commit()

    return ttlCnt


def compressToDays(cursor, dayList):
    """
        Args: (mysql obj): cur, mysql cursor object
              (list): daysList, list of days to compress from hourly
        Desc: Automatically compresses hours per day into single day point for
              rid,logdate,location,data_type
    """
    for day in dayList:
        logging.info("Compressing " + day.strftime('%Y-%m-%d'))
        try:
            cursor.execute("REPLACE INTO stats_daily\
            (rid,logdate,count,min_time,ave_time,max_time,min_client_time,\
            ave_client_time,max_client_time,ave_bytes,\
            max_bytes,ave_rows,max_rows,fails,location,\
            data_type) \
            SELECT \
            rid,\
            logdate,\
            SUM(count) AS count,\
            MIN(min_time) AS min_time,\
            SUM(ave_time*count)/SUM(count) AS ave_time,\
            MAX(max_time) AS max_time,\
            MIN(min_client_time) AS min_client_time,\
            SUM(ave_client_time*count)/SUM(count) AS ave_client_time,\
            MAX(max_client_time) AS max_client_time,\
            SUM(ave_bytes*count)/SUM(count) AS ave_bytes,\
            MAX(max_bytes) AS max_bytes,\
            SUM(ave_rows*count)/SUM(count) AS ave_rows,\
            MAX(max_rows) AS max_rows,\
            SUM(fails) AS fails,\
            location,\
            data_type\
            FROM stats_hourly WHERE logdate=%s\
            GROUP BY rid,logdate,location,data_type",
            day.strftime('%Y-%m-%d')
            )
        except MySQLdb.Error, e:
            logging.error("Failed to compress stats_hourly into stats_daily for"
                          " day: " + day.strfTime('%Y-%m-%d'))


def topX(cursor, dtList, hostD):
    """
        Args: (mysql obj): cur, mysql cursor object
              (list): dtList, list of days to compress from hourly
              (dict): hostD, collection of hosts and info, dict of dict
        Desc: COUNT VOLUME
              Collects, ranks, and stores top RIDs according to various params
              Four total computations:
                 by date,system
                 by date,system,location
                 by date,system,data_type
                 by date,system,data_type,location
    """   
    dTypes = {}
    systems = {}
    cursor.execute("SELECT id,name FROM data_type")
    res = cursor.fetchall()
    for row in res:
        dTypes[row[1]] = row[0]           
 
    cursor.execute("SELECT id,name FROM system")
    res = cursor.fetchall()
    for row in res:
        systems[row[1]] = str(row[0])

    for day in dtList:
        logging.info("Computing all top 20 COUNT sets for "
                     "day " + day.strftime('%Y-%m-%d'))
        for s in systems:

            #### COMPUTE by system only #######################################
            cursor.execute("SELECT s.rid,s.logdate,s.location \
            FROM stats_daily s, requests r WHERE s.logdate=%s \
            AND r.systemid = %s and \
            r.id=s.rid ORDER BY count DESC LIMIT 20", 
              (
                day.strftime('%Y-%m-%d'),
                systems[s]
              )
            )
            res = cursor.fetchall()
            rank = 1
            for row in res:
                cursor.execute("REPLACE INTO top_stats_system \
                (rank,rid,dt,location,systemid,type) VALUES \
                (%s,%s,%s,%s,%s,%s)", 
                  (
                    str(rank),
                    str(row[0]),
                    str(row[1]),
                    'ALL',
                    str(systems[s]),
                    1                #type 1 = count
                  )
                )
                rank += 1
            
            for h in hostD:
                hostloc = hostD[h]["location"]

                #### COMPUTE by system, location ##############################
                cursor.execute("SELECT s.rid,s.logdate,s.location  \
                FROM stats_daily s, requests r WHERE s.logdate=%s AND \
                s.location=%s AND r.systemid = %s AND \
                r.id=s.rid ORDER BY count DESC LIMIT 20", 
                  (
                    day.strftime('%Y-%m-%d'),
                    hostloc,
                    systems[s]
                  )
                )
                res = cursor.fetchall()
                rank = 1
                for row in res:
                    cursor.execute("REPLACE INTO top_stats_system \
                    (rank,rid,dt,location,systemid,type) VALUES \
                    (%s,%s,%s,%s,%s,%s)", 
                      (
                        str(rank),
                        str(row[0]),
                        str(row[1]),
                        str(row[2]),
                        str(systems[s]),
                        1                #type 1 = count
                      )
                    )
                    rank += 1

            for t in dTypes:
                #### COMPUTE by system, data type #############################
                cursor.execute("SELECT s.rid,s.logdate,s.location,s.data_type \
                FROM stats_daily s, requests r WHERE s.logdate=%s \
                AND s.data_type=%s AND r.systemid = %s AND \
                r.id=s.rid ORDER BY count DESC LIMIT 20", 
                  (
                    day.strftime('%Y-%m-%d'),
                    dTypes[t],
                    systems[s]
                  )
                )
                res = cursor.fetchall()
                rank = 1
                for row in res:
                    cursor.execute("REPLACE INTO top_stats_data \
                    (rank,rid,dt,location,data_type,systemid,type) VALUES \
                    (%s,%s,%s,%s,%s,%s,%s)", 
                      (
                        str(rank),
                        str(row[0]),
                        str(row[1]),
                        'ALL',
                        str(row[3]),
                        str(systems[s]),
                        1                #type 1 = count
                      )
                    )
                    rank += 1

                for h in hostD:
                    hostloc = hostD[h]["location"]
      
                    #### COMPUTE by system, data type, and location ###########
                    cursor.execute("SELECT s.rid,s.logdate,s.location,\
                    s.data_type FROM stats_daily s, requests r WHERE \
                    s.logdate=%s AND s.location=%s AND s.data_type=%s \
                    AND r.systemid = %s AND r.id=s.rid \
                    ORDER BY count DESC LIMIT 20", 
                      (
                        day.strftime('%Y-%m-%d'),
                        hostloc,
                        dTypes[t],
                        systems[s]
                      )
                    )
                    res = cursor.fetchall()
                    rank = 1
                    for row in res:
                        cursor.execute("REPLACE INTO top_stats_data \
                        (rank,rid,dt,location,data_type,systemid,type) VALUES \
                        (%s,%s,%s,%s,%s,%s,%s)", 
                          (
                            str(rank),
                            str(row[0]),
                            str(row[1]),
                            str(row[2]),
                            str(row[3]),
                            str(systems[s]),
                            1                #type 1 = count
                          )
                        )
                        rank += 1

def topXLoad(cursor, dtList, hostD):
    """
        Args: (mysql obj): cur, mysql cursor object
              (list): dtList, list of days to compress from hourly
              (dict): hostD, collection of hosts and info, dict of dict
        Desc: LOAD (count * ave_bytes)
              Collects, ranks, and stores top RIDs according to various params
              Four total computations:
                 by date,system
                 by date,system,location
                 by date,system,data_type
                 by date,system,data_type,location
    """   
    dTypes = {}
    systems = {}
    cursor.execute("SELECT id,name FROM data_type")
    res = cursor.fetchall()
    for row in res:
        dTypes[row[1]] = row[0]           
 
    cursor.execute("SELECT id,name FROM system")
    res = cursor.fetchall()
    for row in res:
        systems[row[1]] = str(row[0])

    for day in dtList:
        logging.info("Computing all top 20 LOAD sets for "
                     "day " + day.strftime('%Y-%m-%d'))
        for s in systems:

            #### COMPUTE by system only #######################################
            cursor.execute("SELECT s.rid,s.logdate,s.location \
            FROM stats_daily s, requests r WHERE s.logdate=%s \
            AND r.systemid = %s and \
            r.id=s.rid ORDER BY s.count*s.ave_bytes DESC LIMIT 20", 
              (
                day.strftime('%Y-%m-%d'),
                systems[s]
              )
            )
            res = cursor.fetchall()
            rank = 1
            for row in res:
                cursor.execute("REPLACE INTO top_stats_system \
                (rank,rid,dt,location,systemid,type) VALUES \
                (%s,%s,%s,%s,%s,%s)", 
                  (
                    str(rank),
                    str(row[0]),
                    str(row[1]),
                    'ALL',
                    str(systems[s]),
                    2                #type 2 = load
                  )
                )
                rank += 1
            
            for h in hostD:
                hostloc = hostD[h]["location"]

                #### COMPUTE by system, location ##############################
                cursor.execute("SELECT s.rid,s.logdate,s.location  \
                FROM stats_daily s, requests r WHERE s.logdate=%s AND \
                s.location=%s AND r.systemid = %s AND \
                r.id=s.rid ORDER BY s.count*s.ave_bytes DESC LIMIT 20", 
                  (
                    day.strftime('%Y-%m-%d'),
                    hostloc,
                    systems[s]
                  )
                )
                res = cursor.fetchall()
                rank = 1
                for row in res:
                    cursor.execute("REPLACE INTO top_stats_system \
                    (rank,rid,dt,location,systemid,type) VALUES \
                    (%s,%s,%s,%s,%s,%s)", 
                      (
                        str(rank),
                        str(row[0]),
                        str(row[1]),
                        str(row[2]),
                        str(systems[s]),
                        2                #type 2 = load
                      )
                    )
                    rank += 1

            for t in dTypes:
                #### COMPUTE by system, data type #############################
                cursor.execute("SELECT s.rid,s.logdate,s.location,s.data_type \
                FROM stats_daily s, requests r WHERE s.logdate=%s \
                AND s.data_type=%s AND r.systemid = %s AND \
                r.id=s.rid ORDER BY s.count*s.ave_bytes DESC LIMIT 20", 
                  (
                    day.strftime('%Y-%m-%d'),
                    dTypes[t],
                    systems[s]
                  )
                )
                res = cursor.fetchall()
                rank = 1
                for row in res:
                    cursor.execute("REPLACE INTO top_stats_data \
                    (rank,rid,dt,location,data_type,systemid,type) VALUES \
                    (%s,%s,%s,%s,%s,%s,%s)", 
                      (
                        str(rank),
                        str(row[0]),
                        str(row[1]),
                        'ALL',
                        str(row[3]),
                        str(systems[s]),
                        2                #type 2 = load
                      )
                    )
                    rank += 1

                for h in hostD:
                    hostloc = hostD[h]["location"]
      
                    #### COMPUTE by system, data type, and location ###########
                    cursor.execute("SELECT s.rid,s.logdate,s.location,\
                    s.data_type FROM stats_daily s, requests r WHERE \
                    s.logdate=%s AND s.location=%s AND s.data_type=%s \
                    AND r.systemid = %s AND r.id=s.rid \
                    ORDER BY s.count*s.ave_bytes DESC LIMIT 20", 
                      (
                        day.strftime('%Y-%m-%d'),
                        hostloc,
                        dTypes[t],
                        systems[s]
                      )
                    )
                    res = cursor.fetchall()
                    rank = 1
                    for row in res:
                        cursor.execute("REPLACE INTO top_stats_data \
                        (rank,rid,dt,location,data_type,systemid,type) VALUES \
                        (%s,%s,%s,%s,%s,%s,%s)", 
                          (
                            str(rank),
                            str(row[0]),
                            str(row[1]),
                            str(row[2]),
                            str(row[3]),
                            str(systems[s]),
                            2                #type 2 = load
                          )
                        )
                        rank += 1

def topXDuration(cursor, dtList, hostD):
    """
        Args: (mysql obj): cur, mysql cursor object
              (list): dtList, list of days to compress from hourly
              (dict): hostD, collection of hosts and info, dict of dict
        Desc: DURATION (count * ave_time)
              Collects, ranks, and stores top RIDs according to various params
              Four total computations:
                 by date,system
                 by date,system,location
                 by date,system,data_type
                 by date,system,data_type,location
    """   
    dTypes = {}
    systems = {}
    cursor.execute("SELECT id,name FROM data_type")
    res = cursor.fetchall()
    for row in res:
        dTypes[row[1]] = row[0]           
 
    cursor.execute("SELECT id,name FROM system")
    res = cursor.fetchall()
    for row in res:
        systems[row[1]] = str(row[0])

    for day in dtList:
        logging.info("Computing all top 20 DURATION sets for "
                     "day " + day.strftime('%Y-%m-%d'))
        for s in systems:

            #### COMPUTE by system only #######################################
            cursor.execute("SELECT s.rid,s.logdate,s.location \
            FROM stats_daily s, requests r WHERE s.logdate=%s \
            AND r.systemid = %s and \
            r.id=s.rid ORDER BY s.count*s.ave_time DESC LIMIT 20", 
              (
                day.strftime('%Y-%m-%d'),
                systems[s]
              )
            )
            res = cursor.fetchall()
            rank = 1
            for row in res:
                cursor.execute("REPLACE INTO top_stats_system \
                (rank,rid,dt,location,systemid,type) VALUES \
                (%s,%s,%s,%s,%s,%s)", 
                  (
                    str(rank),
                    str(row[0]),
                    str(row[1]),
                    'ALL',
                    str(systems[s]),
                    3                #type 3 = duration
                  )
                )
                rank += 1
            
            for h in hostD:
                hostloc = hostD[h]["location"]

                #### COMPUTE by system, location ##############################
                cursor.execute("SELECT s.rid,s.logdate,s.location  \
                FROM stats_daily s, requests r WHERE s.logdate=%s AND \
                s.location=%s AND r.systemid = %s AND \
                r.id=s.rid ORDER BY s.count*s.ave_time DESC LIMIT 20", 
                  (
                    day.strftime('%Y-%m-%d'),
                    hostloc,
                    systems[s]
                  )
                )
                res = cursor.fetchall()
                rank = 1
                for row in res:
                    cursor.execute("REPLACE INTO top_stats_system \
                    (rank,rid,dt,location,systemid,type) VALUES \
                    (%s,%s,%s,%s,%s,%s)", 
                      (
                        str(rank),
                        str(row[0]),
                        str(row[1]),
                        str(row[2]),
                        str(systems[s]),
                        3                #type 3 = duration
                      )
                    )
                    rank += 1

            for t in dTypes:
                #### COMPUTE by system, data type #############################
                cursor.execute("SELECT s.rid,s.logdate,s.location,s.data_type \
                FROM stats_daily s, requests r WHERE s.logdate=%s \
                AND s.data_type=%s AND r.systemid = %s AND \
                r.id=s.rid ORDER BY s.count*s.ave_time DESC LIMIT 20", 
                  (
                    day.strftime('%Y-%m-%d'),
                    dTypes[t],
                    systems[s]
                  )
                )
                res = cursor.fetchall()
                rank = 1
                for row in res:
                    cursor.execute("REPLACE INTO top_stats_data \
                    (rank,rid,dt,location,data_type,systemid,type) VALUES \
                    (%s,%s,%s,%s,%s,%s,%s)", 
                      (
                        str(rank),
                        str(row[0]),
                        str(row[1]),
                        'ALL',
                        str(row[3]),
                        str(systems[s]),
                        3                #type 3 = duration
                      )
                    )
                    rank += 1

                for h in hostD:
                    hostloc = hostD[h]["location"]
      
                    #### COMPUTE by system, data type, and location ###########
                    cursor.execute("SELECT s.rid,s.logdate,s.location,\
                    s.data_type FROM stats_daily s, requests r WHERE \
                    s.logdate=%s AND s.location=%s AND s.data_type=%s \
                    AND r.systemid = %s AND r.id=s.rid \
                    ORDER BY s.count*s.ave_time DESC LIMIT 20", 
                      (
                        day.strftime('%Y-%m-%d'),
                        hostloc,
                        dTypes[t],
                        systems[s]
                      )
                    )
                    res = cursor.fetchall()
                    rank = 1
                    for row in res:
                        cursor.execute("REPLACE INTO top_stats_data \
                        (rank,rid,dt,location,data_type,systemid,type) VALUES \
                        (%s,%s,%s,%s,%s,%s,%s)", 
                          (
                            str(rank),
                            str(row[0]),
                            str(row[1]),
                            str(row[2]),
                            str(row[3]),
                            str(systems[s]),
                            3                #type 3 = duration
                          )
                        )
                        rank += 1

def topXService(cursor, dtList, hostD, serviceD):
    dTypes = {}
    cursor.execute("SELECT id,name FROM data_type")
    res = cursor.fetchall()
    for row in res:
        dTypes[row[1]] = row[0]
    
    for day in dtList:
        logging.info("Computing all top 20 service sets for "
                     "day " + day.strftime('%Y-%m-%d'))
        for system in serviceD:
            for service in system:
    
                for t in dTypes:
                    #### COUNT ####################################################
                    #### COMPUTE by system, data type #############################
                    cursor.execute("SELECT s.rid,s.logdate,s.location,s.data_type \
                    FROM stats_daily s, requests r WHERE s.logdate=%s \
                    AND s.data_type=%s AND r.systemid = %s AND r.service = %s AND \
                    r.id=s.rid ORDER BY s.count DESC LIMIT 20", 
                      (
                        day.strftime('%Y-%m-%d'),
                        dTypes[t],
                        str(system),
                        str(service)
                      )
                    )
                    res = cursor.fetchall()
                    rank = 1
                    for row in res:
                        cursor.execute("REPLACE INTO top_stats_service_data \
                        (rank,rid,dt,location,data_type,systemid,type,service) VALUES \
                        (%s,%s,%s,%s,%s,%s,%s,%s)", 
                          (
                            str(rank),
                            str(row[0]),
                            str(row[1]),
                            'ALL',
                            str(row[3]),
                            str(system),
                            1,                
                            str(service)
                          )
                        )
                        rank += 1
                    #### LOAD #####################################################    
                    #### COMPUTE by system, data type #############################
                    cursor.execute("SELECT s.rid,s.logdate,s.location,s.data_type \
                    FROM stats_daily s, requests r WHERE s.logdate=%s \
                    AND s.data_type=%s AND r.systemid = %s AND r.service = %s AND \
                    r.id=s.rid ORDER BY s.count*s.ave_bytes DESC LIMIT 20", 
                      (
                        day.strftime('%Y-%m-%d'),
                        dTypes[t],
                        str(system),
                        str(service)
                      )
                    )
                    res = cursor.fetchall()
                    rank = 1
                    for row in res:
                        cursor.execute("REPLACE INTO top_stats_service_data \
                        (rank,rid,dt,location,data_type,systemid,type,service) VALUES \
                        (%s,%s,%s,%s,%s,%s,%s,%s)", 
                          (
                            str(rank),
                            str(row[0]),
                            str(row[1]),
                            'ALL',
                            str(row[3]),
                            str(system),
                            2,                
                            str(service)
                          )
                        )
                        rank += 1
                    #### DURATION #################################################
                    #### COMPUTE by system, data type #############################
                    cursor.execute("SELECT s.rid,s.logdate,s.location,s.data_type \
                    FROM stats_daily s, requests r WHERE s.logdate=%s \
                    AND s.data_type=%s AND r.systemid = %s AND r.service = %s AND \
                    r.id=s.rid ORDER BY s.count*s.ave_time DESC LIMIT 20", 
                      (
                        day.strftime('%Y-%m-%d'),
                        dTypes[t],
                        str(system),
                        str(service)
                      )
                    )
                    res = cursor.fetchall()
                    rank = 1
                    for row in res:
                        cursor.execute("REPLACE INTO top_stats_service_data \
                        (rank,rid,dt,location,data_type,systemid,type,service) VALUES \
                        (%s,%s,%s,%s,%s,%s,%s,%s)", 
                          (
                            str(rank),
                            str(row[0]),
                            str(row[1]),
                            'ALL',
                            str(row[3]),
                            str(system),
                            3,                
                            str(service)
                          )
                        )
                        rank += 1
                    ##############################################################
                    ##############################################################
                    for h in hostD:
                        hostloc = hostD[h]["location"]
                        #### COUNT ################################################
                        #### COMPUTE by system, data type, and location ###########
                        cursor.execute("SELECT s.rid,s.logdate,s.location,\
                        s.data_type FROM stats_daily s, requests r WHERE \
                        s.logdate=%s AND s.location=%s AND s.data_type=%s \
                        AND r.systemid = %s AND r.id=s.rid AND r.service=%s \
                        ORDER BY s.count DESC LIMIT 20", 
                          (
                            day.strftime('%Y-%m-%d'),
                            hostloc,
                            dTypes[t],
                            str(system),
                            str(service)
                          )
                        )
                        res = cursor.fetchall()
                        rank = 1
                        for row in res:
                            cursor.execute("REPLACE INTO top_stats_service_data \
                            (rank,rid,dt,location,data_type,systemid,type,service) VALUES \
                            (%s,%s,%s,%s,%s,%s,%s,%s)", 
                              (
                                str(rank),
                                str(row[0]),
                                str(row[1]),
                                str(row[2]),
                                str(row[3]),
                                str(system),
                                1,                
                                str(service)
                              )
                            )
                            rank += 1
                        #### LOAD ####################################################    
                        #### COMPUTE by system, data type, and location ###########
                        cursor.execute("SELECT s.rid,s.logdate,s.location,\
                        s.data_type FROM stats_daily s, requests r WHERE \
                        s.logdate=%s AND s.location=%s AND s.data_type=%s \
                        AND r.systemid = %s AND r.id=s.rid AND r.service=%s \
                        ORDER BY s.count*s.ave_bytes DESC LIMIT 20", 
                          (
                            day.strftime('%Y-%m-%d'),
                            hostloc,
                            dTypes[t],
                            str(system),
                            str(service)
                          )
                        )
                        res = cursor.fetchall()
                        rank = 1
                        for row in res:
                            cursor.execute("REPLACE INTO top_stats_service_data \
                            (rank,rid,dt,location,data_type,systemid,type,service) VALUES \
                            (%s,%s,%s,%s,%s,%s,%s,%s)", 
                              (
                                str(rank),
                                str(row[0]),
                                str(row[1]),
                                str(row[2]),
                                str(row[3]),
                                str(system),
                                2,                
                                str(service)
                              )
                            )
                            rank += 1
                        #### DURATION ####################################################  
                        #### COMPUTE by system, data type, and location ###########
                        cursor.execute("SELECT s.rid,s.logdate,s.location,\
                        s.data_type FROM stats_daily s, requests r WHERE \
                        s.logdate=%s AND s.location=%s AND s.data_type=%s \
                        AND r.systemid = %s AND r.id=s.rid AND r.service=%s \
                        ORDER BY s.count*s.ave_time DESC LIMIT 20", 
                          (
                            day.strftime('%Y-%m-%d'),
                            hostloc,
                            dTypes[t],
                            str(system),
                            str(service)
                          )
                        )
                        res = cursor.fetchall()
                        rank = 1
                        for row in res:
                            cursor.execute("REPLACE INTO top_stats_service_data \
                            (rank,rid,dt,location,data_type,systemid,type,service) VALUES \
                            (%s,%s,%s,%s,%s,%s,%s,%s)", 
                              (
                                str(rank),
                                str(row[0]),
                                str(row[1]),
                                str(row[2]),
                                str(row[3]),
                                str(system),
                                3,                
                                str(service)
                              )
                            )
                            rank += 1
                        

def movingAverage(cursor, dtList):
    
    firstDay = min(dtList).strftime('%Y-%m-%d')
    lastDay = max(dtList).strftime('%Y-%m-%d')
    logging.info("Computing moving 30 day avgs for "
                 "range " + firstDay + " to " + lastDay)
    
    #count volume
    cursor.execute("REPLACE INTO moving_avgs \
    (rid,dt,avg_range,location,data_type,type,value) \
    SELECT s.rid,s.logdate,30,s.location,s.data_type,1,\
    (SELECT ROUND(SUM(count)/30) FROM stats_daily WHERE rid=s.rid AND \
    location=s.location AND data_type=s.data_type AND logdate BETWEEN \
    DATE_SUB(s.logdate,INTERVAL 30 DAY) AND s.logdate \
    GROUP BY rid,location,data_type) FROM stats_daily s \
    WHERE logdate BETWEEN %s AND %s; ",(firstDay,lastDay))
    
    #duration (sec)
    cursor.execute("REPLACE INTO moving_avgs \
    (rid,dt,avg_range,location,data_type,type,value) \
    SELECT s.rid,s.logdate,30,s.location,s.data_type,3,\
    (SELECT ROUND(SUM(count*ave_time)/30) \
    FROM stats_daily WHERE rid=s.rid AND \
    location=s.location AND data_type=s.data_type AND logdate BETWEEN \
    DATE_SUB(s.logdate,INTERVAL 30 DAY) AND s.logdate \
    GROUP BY rid,location,data_type) FROM stats_daily s \
    WHERE logdate BETWEEN %s AND %s; ",(firstDay,lastDay))
    
    #kilobytes
    cursor.execute("REPLACE INTO moving_avgs \
    (rid,dt,avg_range,location,data_type,type,value) \
    SELECT s.rid,s.logdate,30,s.location,s.data_type,2,\
    (SELECT ROUND(SUM(count*ave_bytes)/30) \
    FROM stats_daily WHERE rid=s.rid AND \
    location=s.location AND data_type=s.data_type AND logdate BETWEEN \
    DATE_SUB(s.logdate,INTERVAL 30 DAY) AND s.logdate \
    GROUP BY rid,location,data_type) FROM stats_daily s \
    WHERE logdate BETWEEN %s AND %s; ",(firstDay,lastDay))
    

def transferFile(host,filename,toDir):
    """
        Args: (string):host, hostname(actual address according to hosts file)
              (string):filename, the filename (including directory) to be 
                       copied over
              (string):toDir, the filename and directory to be copied into/as
     Returns: 1 on success, 0 on failure.
        Desc: rcp's the target file into the target directory as the given name
    """   
    fromHost = str(host) + ":" + str(filename)
    logging.info("Transferring " + fromHost)
    try:
        ret = subprocess.Popen([
                                "/usr/bin/rsync",#"/usr/bin/rcp",
                                "-az",
                                "--rsh=rsh",
                                fromHost, 
                                toDir 
                                ], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        
        
        x = ret.communicate()
        if x[1]:
            if not re.match(r'.*\.gz',filename):
                return transferFile(host,str(filename)+'.gz',str(toDir)+'.gz')
            logging.warning("transfer returned: " + x[1])
            return 0
        if re.match(r'.*\.gz',filename):
            _execute('/usr/bin/gunzip {0}'.format(toDir))
        return 1
    except(OSError,ValueError,IOError):
        logging.error("Failed to transfer logdir from host: " + host)
        return 0


def transferFileHttp(fileDir, targFile, toDir):
    """
        Assumes filename does not include gz
        Assumes you are gzipping the logs
    """
    logging.info("Transferring {0}/{1}.gz".format(fileDir,targFile))
    
    try:
        resp = urllib2.urlopen('{0}/{1}.gz'.format(fileDir,targFile))
    except urllib2.HTTPError as e:
        logging.error("Failed to transfer file over web from: {0}/{1}.gz".format(fileDir,targFile))
        return 0
    if resp.code != 200:
        logging.error("Failed to transfer file over web from: {0}/{1}.gz got response code {2}".format(fileDir,targFile,resp.code))
        return 0
    page = resp.read()
    
    # write out the zipped file
    fp = open('{0}.gz'.format(toDir),'wb')
    fp.write(page)
    fp.flush()
    fp.close()
    # unzip that bastard
    _execute('/usr/bin/gunzip {0}.gz'.format(toDir))
    return 1
    

def _execute(command, currentDir=None):
    cmdList = shlex.split(command)
    try:
        x = subprocess.Popen(cmdList,cwd=currentDir,stdout=subprocess.PIPE,stderr=subprocess.PIPE).communicate()
        if x[1]:
            logging.warning("EXECUTE returned: {0}For command {1}".format(x[1],command))
            return 0
        return 1
    except(OSError,ValueError,IOError) as e:
        logging.error("Failed to execute command {0}".format(command))
        logging.error("Exception caught: {0}".format(e))
        return 0
    

if __name__ == '__main__':
    main()




