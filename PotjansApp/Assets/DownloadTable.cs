using System;
using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Threading;
using UnityEngine;
using UnityEngine.UI;

public class DownloadTable : MonoBehaviour {

    [SerializeField] private string url;
    [SerializeField] private Transform contenTransform;
    [SerializeField] private Text lessonPrefab;

    private Day[] days;

    public static DownloadTable instance;

    private struct Day {
        public bool hasDate;
        public string info;
        public DateTime date;
        public Lesson[] duelmen;
        public Lesson[] buldern;
        public Lesson[] hausduelmen;

        public Day(string _date, Lesson[] _duelmen, Lesson[] _buldern, Lesson[] _hausduelmen) {
            if (_date.Substring(5).Split('.').Length == 3) {
                //its a real date
                string dateSubstring = _date.Substring(5);
                string[] dateSplit = dateSubstring.Split('.');
                int year = int.Parse(dateSplit[2]);
                int month = int.Parse(dateSplit[1]);
                int day = int.Parse(dateSplit[0]);
                date = new DateTime(year, month, day);
                hasDate = true;
                info = "";
            }
            else {
                //something else
                date = new DateTime(1970, 1, 1);
                hasDate = false;
                info = _date;
            }
            
            duelmen = _duelmen;
            buldern = _buldern;
            hausduelmen = _hausduelmen;
        }
    }

    private struct Lesson {
        public string time;
        public string topic;

        public Lesson(string _time, string _topic) {
            time = _time;
            topic = _topic;
        }
    }

	// Use this for initialization
	void Start () {
	    if (instance == null) {
	        instance = this;
	    }
	    else {
	        Debug.LogError("Multiple instances");
	    }
	    
        refreshData();

	}

    public void loadNewData(string location) {
        clearDisplayedData();
        displayData(location);
        //TODO: Auto jump to current date entery or next best
    }

    private void clearDisplayedData() {
        foreach (Transform t in contenTransform) {
            Destroy(t.gameObject);
        }
    }

    private void displayData(string location) {
        foreach (Day day in days) {
            Text txtTime = Instantiate(lessonPrefab, contenTransform);
            if (day.hasDate) {
                CultureInfo culture = new CultureInfo("de-DE");
                txtTime.text = culture.DateTimeFormat.GetDayName(day.date.DayOfWeek) + ", " + day.date.Date.ToString("d", culture);
                txtTime.fontStyle = FontStyle.Bold;
            }
            else {
                txtTime.text = day.info;
                txtTime.fontStyle = FontStyle.Bold;
                txtTime.fontStyle = FontStyle.BoldAndItalic;
            }
            

            switch (location)
            {
                case "duelmen":
                    foreach (Lesson lesson in day.duelmen)
                    {
                        if (lesson.topic != null)
                        {
                            if (lesson.topic.Equals("kein Unterricht"))
                            {
                                if (day.hasDate)
                                {
                                    Destroy(txtTime.gameObject);
                                    continue;
                                }
                            }
                        }
                        if (string.IsNullOrEmpty(lesson.time) && string.IsNullOrEmpty(lesson.topic))
                        {
                            if (day.hasDate) {
                                Destroy(txtTime.gameObject);
                            }
                            continue;
                        }
                        Text txtDuelmen = Instantiate(lessonPrefab, contenTransform);
                        txtDuelmen.text = lesson.time + " " + lesson.topic;
                    }
                    break;
                case "buldern":
                    foreach (Lesson lesson in day.buldern)
                    {
                        if (lesson.topic != null)
                        {
                            if (lesson.topic.Equals("kein Unterricht"))
                            {
                                if (day.hasDate)
                                {
                                    Destroy(txtTime.gameObject);
                                    continue;
                                }
                            }
                        }
                        if (string.IsNullOrEmpty(lesson.time) && string.IsNullOrEmpty(lesson.topic))
                        {
                            if (day.hasDate)
                            {
                                Destroy(txtTime.gameObject);
                            }
                            continue;
                        }
                        Text txtDuelmen = Instantiate(lessonPrefab, contenTransform);
                        txtDuelmen.text = lesson.time + " " + lesson.topic;
                    }
                    break;

                case "hausduelmen":
                    foreach (Lesson lesson in day.hausduelmen)
                    {
                        if (lesson.topic != null)
                        {
                            if (lesson.topic.Equals("kein Unterricht"))
                            {
                                if (day.hasDate)
                                {
                                    Destroy(txtTime.gameObject);
                                    continue;
                                }
                            }
                        }
                        if (string.IsNullOrEmpty(lesson.time) && string.IsNullOrEmpty(lesson.topic))
                        {
                            if (day.hasDate)
                            {
                                Destroy(txtTime.gameObject);
                            }
                            continue;
                        }
                        Text txtDuelmen = Instantiate(lessonPrefab, contenTransform);
                        txtDuelmen.text = lesson.time + " " + lesson.topic;
                    }
                    break;
                default:
                    break;
            }


        }
    }

    private void refreshData() {
        bool succ;
        string htmlData = download(out succ);
        if (!succ) {
            return;
        }
        days = convertToStruct(htmlData);

        foreach (Day day in days) {
            string d = day.duelmen.Length + " -> ";
            string b = day.buldern.Length + " -> ";
            string h = day.hausduelmen.Length + " -> ";
            foreach (Lesson lesson in day.duelmen) {
                d += "[" + lesson.time + " | " + lesson.topic + "]";
            }
            foreach (Lesson lesson in day.buldern)
            {
                b += "[" + lesson.time + " | " + lesson.topic + "]";
            }
            foreach (Lesson lesson in day.hausduelmen)
            {
                h += "[" + lesson.time + " | " + lesson.topic + "]";
            }
            Debug.Log(day.date + " " + d + " ... " + b + " ... " + h);
        }
    }

    private string download(out bool succ) {
        WWW www = new WWW(url);
        while (!www.isDone) {
            //Debug.Log(www.progress);
        }
        if (string.IsNullOrEmpty(www.error)) {
            succ = true;
            return www.text;
        }
        Debug.LogError("Failed to download");
        succ = false;
        return "";
    }

    private Day[] convertToStruct(string _htmlData) {
        string table = extractTable(_htmlData);
        Debug.Log("Usefull: " + table);

        string[] tmpdays = table.Split(new [] { "  <tr>" }, StringSplitOptions.RemoveEmptyEntries);
        Day[] days = new Day[tmpdays.Length];
        int lastGotAddedToDayBefore = 1;
        int daysI = -1;
        for (int i = 0; i < tmpdays.Length; i++)
        {
            string tmp = tmpdays[i];
            string[] data = new string[7];
            for (int j = 0; j < 7; j++) {
                tmp = tmp.Substring(4);
                data[j] = getBetween(tmp, "<td>", "</td>");

                tmp = tmp.Substring(9 + data[j].Length);
            }
            if (data[0].Equals("")) {
                Debug.Log("---" + daysI);
                days[daysI].duelmen = addLessonIfNeeded(days[daysI].duelmen, data[1], data[2]);
                days[daysI].buldern = addLessonIfNeeded(days[daysI].buldern, data[3], data[4]);
                days[daysI].hausduelmen = addLessonIfNeeded(days[daysI].hausduelmen, data[6], data[6]);
            }
            else
            {
                daysI++;
                Debug.Log(daysI + " " + data[0]);
                days[daysI] = new Day(data[0], new Lesson[] { new Lesson(data[1], data[2]) }, new Lesson[] { new Lesson(data[3], data[4]) }, new Lesson[] { new Lesson(data[5], data[6]) });
            }
        }
        days = days.Where(_day => _day.duelmen != null && _day.buldern != null && _day.hausduelmen != null).ToArray();
        return days;
    }

    private static string extractTable(string _htmlData) {
        string table = getBetween(_htmlData, "<table width=\"100%\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\">", "</table>");
        table = table.Substring(table.IndexOf("  <tr>"));
        table = table.Replace("\n", "");
        table = table.Replace("\r", "");
        table = table.Replace(
            "  <tr>    <td height=\"25\"></td>    <td height=\"25\" bgcolor=\"#FFFFAE\"><div align=\"center\"></div></td>    <td height=\"25\" bgcolor=\"#FFFFAE\"><div align=\"center\"></div></td>    <td height=\"25\" bgcolor=\"#D9D9FF\"><div align=\"center\"></div></td>    <td height=\"25\" bgcolor=\"#D9D9FF\"><div align=\"center\"></div></td>    <td height=\"25\" bgcolor=\"#D3F1D3\"><div align=\"center\"></div></td>    <td height=\"25\" bgcolor=\"#D3F1D3\"><div align=\"center\"></div></td>  </tr>",
            "");
        table = table.Replace(" width=\"13%\"", "");
        table = table.Replace(" width=\"12%\"", "");
        table = table.Replace(" width=\"25%\"", "");
        table = table.Replace(" height=\"25\"", "");
        table = table.Replace(" bgcolor=\"#FFFFAE\"", "");
        table = table.Replace(" bgcolor=\"#D9D9FF\"", "");
        table = table.Replace(" bgcolor=\"#D3F1D3\"", "");
        table = table.Replace("<div align=\"center\">", "");
        table = table.Replace("</div>", "");
        table = table.Replace("&nbsp;", "");
        table = table.Replace("  </tr>", "");
        return table;
    }

    private Lesson[] addLessonIfNeeded(Lesson[] _old, string _time, string _topic) {
        if (_time.Equals("") && _topic.Equals("")) return _old;
        Lesson[] newLessons = new Lesson[_old.Length + 1];
        Lesson[] lesson = new Lesson[] { new Lesson(_time, _topic) };
        _old.CopyTo(newLessons, 0);
        lesson.CopyTo(newLessons, _old.Length);
        return newLessons;
    } 


    public static string getBetween(string strSource, string strStart, string strEnd)
    {
        if (strSource.Contains(strStart) && strSource.Contains(strEnd))
        {
            int Start = strSource.IndexOf(strStart, 0) + strStart.Length;
            int End = strSource.IndexOf(strEnd, Start);
            return strSource.Substring(Start, End - Start);
        }
        else
        {
            return "";
        }
    }
}

