import sys
import re
import csv

from twill import get_browser
from twill.commands import fv, code

KINDS = ["Device", "SurveyInstance", "Survey"]

AUTH_URL = "https://accounts.google.com/ServiceLogin"
BASE_URL = "https://appengine.google.com/datastore/statistics?app_id=s~"

count_regex = re.compile(r"<td>Entry count:</td>\s*<td>([0-9,]+)</td>")
browser = get_browser()

def read_instances(filename):
    with open(filename) as f:
        instances = [line.strip() for line in f.readlines()]
    return instances

def authenticate(email, pwd):
    print "Authenticating Google Account: %s" % email
    browser.go(AUTH_URL)
    fv("2", "Email", email)
    fv("2", "Passwd", pwd)
    browser.submit()
    code(200)

def parse_entry_count(instance, kind):
    url = BASE_URL + instance + "&kind=" + kind
    browser.go(url)
    code(200)
    html = browser.get_html()
    result = count_regex.search(html)
    if result:
        return result.group(1).replace(",", "")#Avoid commas
    return "0"

def get_stats(instance):
    print "Getting stats for instance: %s" % instance
    return [parse_entry_count(instance, kind) for kind in KINDS]

def stats():
    email, pwd, config_file = sys.argv[1], sys.argv[2], sys.argv[3]

    # Authenticate with Google Services
    authenticate(email, pwd)

    # Dictionary holding each instance and its correspondent stats list
    stats = {instance: get_stats(instance) for instance in read_instances(config_file)}

    with open("/tmp/stats.txt", "wb") as csvfile:
        statsWriter = csv.writer(csvfile)
        statsWriter.writerow(["Instance"] + KINDS)
        for i in stats.keys():
            statsWriter.writerow([i] + stats[i])
    print "Finished. Stats written to '/tmp/stats.txt'"

if __name__ == "__main__":
    stats()
