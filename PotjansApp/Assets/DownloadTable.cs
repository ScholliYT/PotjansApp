using System;
using System.Collections.Generic;
using System.Globalization;
using System.Text.RegularExpressions;
using UnityEngine;


public class DownloadTable
{
    public static Dictionary<string, Day[]> reloadData()
    {
        bool succ;
        string htmlData = download(out succ);
        if (!succ)
        {
            return null;
        }
        string[] talbeOnly = extractTable(htmlData).Split(new[] { "  <tr>" }, StringSplitOptions.RemoveEmptyEntries);
        return !checkSettings(talbeOnly) ? null : convertToDic(talbeOnly);
    }

    private static string download(out bool _succ)
    {
        WWW www = new WWW(Config.url);
        while (!www.isDone)
        {
            //Debug.Log(www.progress);
        }
        if (string.IsNullOrEmpty(www.error))
        {
            _succ = true;
            Debug.Log("Bytes: " + www.text.Length);
            return www.text;
        }
        Debug.LogError("Failed to download");
        _succ = false;
        return "";
    }

    private static bool checkSettings(string[] _talbeOnly)
    {
        int locationsCount = (Regex.Matches(_talbeOnly[0], "</td>").Count - 1) / 2;
        if (locationsCount == Config.locations.Length) return true;
        Debug.LogWarning("Server: " + locationsCount + " Config: " + Config.locations.Length + " are not the same!");
        return false;
    }

    private static Dictionary<string, Day[]> convertToDic(string[] _talbeOnly)
    {

        Dictionary<string, Day[]> tmpDays = new Dictionary<string, Day[]>();
        //TODO: dont loop over evey location. Data is there. Redundant. Need to fix this
        for (int location = 0; location < Config.locations.Length; location++)
        {
            Debug.Log("<color=white>" + Config.locations[location] + "</color>");
            List<Day> tmpDay = new List<Day>();
            bool skipAdditionalLessonsForThisDay = false;
            foreach (string dDay in _talbeOnly)
            {
                string tmp = dDay.Substring(4);
                string dataDay = StringUtility.getBetween(tmp, "<td>", "</td>");
                tmp = tmp.Substring(9 + dataDay.Length);
                Lesson[] dataLessons = new Lesson[Config.locations.Length];
                for (int loc = 0; loc < Config.locations.Length; loc++)
                {
                    tmp = tmp.Substring(4);
                    string time = StringUtility.getBetween(tmp, "<td>", "</td>");
                    tmp = tmp.Substring(9 + time.Length);

                    tmp = tmp.Substring(4);
                    string topic = StringUtility.getBetween(tmp, "<td>", "</td>");
                    tmp = tmp.Substring(9 + topic.Length);

                    dataLessons[loc] = Lesson.createLesson(time, topic);
                }
                if (dataDay.Equals(""))
                {
                    if (skipAdditionalLessonsForThisDay) continue;
                    NormalDay normalDay = tmpDay[tmpDay.Count - 1] as NormalDay;
                    if (normalDay == null) continue; //should never be true
                    NormalDay day = normalDay;
                    day.lessons = addLessonIfNeeded(day.lessons, dataLessons[location]);
                    Debug.Log("<color=green>" + tmpDay.Count + " " + tmpDay[tmpDay.Count - 1] + "</color>");
                }
                else if (dataDay.Substring(5).Split('.').Length == 3) //its a valid date
                {
                    //string[] dateSplit = dataDay.Substring(5).Split('.');
                    //DateTime dateOfDay = new DateTime(int.Parse(dateSplit[2]), int.Parse(dateSplit[1]), int.Parse(dateSplit[0])); //convert string date into DateTime object
                    DateTime dateOfDay = DateTime.Parse(dataDay.Substring(5), new CultureInfo("de-DE")); //has to be de-DE

                    if (dataLessons[location].topic.Equals("kein Unterricht") || string.IsNullOrEmpty(dataLessons[location].topic) && dataLessons[location].timeIsEmpty())
                    {
                        Debug.Log("<color=black>Kein Unterricht: " + dateOfDay.Date.ToString("D", Config.mainCultureInfo) + "</color>");
                        skipAdditionalLessonsForThisDay = true;
                        continue;
                    }
                    NormalDay day = new NormalDay(dateOfDay, new[] { dataLessons[location] });
                    skipAdditionalLessonsForThisDay = false;
                    tmpDay.Add(day);
                    Debug.Log("<color=blue>" + tmpDay.Count + " " + tmpDay[tmpDay.Count - 1] + "</color>");
                }
                else
                {
                    //its an InfoDay
                    tmpDay.Add(new InfoDay(dataDay));
                    Debug.Log("<color=yellow>" + tmpDay.Count + " " + tmpDay[tmpDay.Count - 1] + "</color>");
                }
            }
            tmpDays.Add(Config.locations[location], tmpDay.ToArray());
        }

        return tmpDays;
    }

    private static string extractTable(string _htmlData)
    {
        string table = StringUtility.getBetween(_htmlData, "<table width=\"100%\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\">", "</table>");
        table = table.Substring(table.IndexOf("  <tr>", StringComparison.Ordinal));
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

    private static Lesson[] addLessonIfNeeded(Lesson[] _old, Lesson _new)
    {
        if (_new.timeIsEmpty() && _new.topic.Equals("")) return _old;
        Lesson[] newLessons = new Lesson[_old.Length + 1];
        Lesson[] lesson = { _new };
        _old.CopyTo(newLessons, 0);
        lesson.CopyTo(newLessons, _old.Length);
        return newLessons;
    }
}

