using System;
using System.Globalization;
using System.Text.RegularExpressions;
using UnityEngine;

public abstract class Day
{
    public abstract string myToString();
    public override string ToString()
    {
        return myToString();
    }
}

public class NormalDay : Day
{
    public DateTime date { get; private set; }
    public Lesson[] lessons { get; set; }

    public NormalDay(DateTime _date, Lesson[] _lessons)
    {
        date = _date;
        lessons = _lessons;
    }

    public override string myToString() {

        string ret = Config.mainCultureInfo.DateTimeFormat.GetDayName(date.Date.DayOfWeek) + ", " + date.Date.ToString("d", Config.mainCultureInfo) + ": ";
        foreach (Lesson lesson in lessons)
        {
            ret += " " + lesson;
        }
        return ret;
    }
}

public class InfoDay : Day
{
    public string info { get; private set; }

    public InfoDay(string _info)
    {
        info = _info.TrimStart().TrimEnd();
    }

    public override string myToString()
    {
        return info;
    }
}

/* Don't use Constructor! Use factory method
 */
public abstract class Lesson
{
    public string topic { get; private set; }

    protected Lesson(string _topic) {
        topic = _topic;
    }

    public abstract string myToString();
    public abstract bool timeIsEmpty();

    public override string ToString()
    {
        return myToString();
    }

    public static Lesson createLesson(string _time, string _topic) {
        
        Regex pattern = new Regex(@"^[0-9]\sh$|^1[0-9]\sh$|^2[0-3]\sh$|^[0-9]\.[0-6][0-9]\sh$|^1[0-9]\.[0-6][0-9]\sh$|^2[0-3]\.[0-6][0-9]\sh$");
        if (!pattern.Match(_time).Success || _time.Equals("")) return new SpecialLesson(_time, _topic);
        _time = _time.Replace(" h", "").Replace(".", ":");
        Debug.Log(_time);
        if (new Regex(@"^[0-9]$").Match(_time).Success) {
            _time = "0" + _time;
            Debug.Log(_time);
        }
        
        DateTime time = DateTime.ParseExact(_time, new[] {"HH", "H:mm", "HH:mm"}, new CultureInfo("de-DE"), DateTimeStyles.None);
        return new NormalLesson(time, _topic);
    }

}

public class NormalLesson : Lesson
{
    public DateTime time { get; private set; }

    public NormalLesson(DateTime _time, string _topic) : base(_topic)
    { 
        time = _time;
    }

    public override string myToString()
    {
        return time.ToString("t", Config.mainCultureInfo) + " " + topic;
    }

    public override bool timeIsEmpty() {
        return false; //if NormalLesson gets created by Lessons Factory it ensures that Time has a valid value
    }
}

public class SpecialLesson : Lesson
{
    public string info { get; private set; }

    public SpecialLesson(string _info, string _topic) : base(_topic)
    {
        info = _info;
    }

    public override string myToString()
    {
        return info + " " + topic;
    }

    public override bool timeIsEmpty() {
        return info.Equals("");
    }
}
