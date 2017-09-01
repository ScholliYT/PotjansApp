using System;
using System.Collections.Generic;
using System.Globalization;
using UnityEngine;
using UnityEngine.UI;
#pragma warning disable 649

public class Manager : MonoBehaviour {

    [SerializeField] private Transform contenTransform;
    [SerializeField] private Text lessonPrefab;

    private Dictionary<string, Day[]> days;

    public static Manager instance;

    // ReSharper disable once UnusedMember.Local
    // ReSharper disable once InconsistentNaming
    // ReSharper disable once ArrangeTypeMemberModifiers
    void Start () {
        if (instance == null) {
            instance = this;
        }
        else {
            Debug.LogError("More than one Manager in scene");
        }

        days = DownloadTable.reloadData();
    }

    public void displayLocalData(string _location)
    {
        clearDisplayedData();
        displayData(_location);

        foreach (Day d in days[_location])
        {
            Debug.Log(d.ToString());
        }
    }

    private void displayData(string _location)
    {
        Day highlightDay = findDayToHighlight(_location, DateTime.Now.Date);
        RectTransform rHighlighDay = displayText(_location, highlightDay);
        //centerScrollViewToItem(rHighlighDay);
        //TODO: Auto jump to rHighlighDay entery
    }

    private NormalDay findDayToHighlight(string _location, DateTime _date)
    {
        Debug.Log("Currently checking date: " + _date.ToString("D"));
        NormalDay d = (NormalDay)Array.Find(days[_location], _day => _day is NormalDay && ((NormalDay)_day).date.Equals(_date.Date));

        if (d == null)
        {
            d = findDayToHighlight(_location, _date.Date.AddDays(1.0));
        }
        else
        {
            Debug.Log("<color=red>" + d.date.ToString("D") + "</color>");
            foreach (Lesson lesson in d.lessons)
            {
                Debug.Log(lesson);
            }
        }
        return d;
    }

    /* Creates the Text UI
     * Returns the RectTransform of the highlighted Day
     */
    private RectTransform displayText(string _location, Day _highlightDay)
    {
        if (!days.ContainsKey(_location))
        {
            Debug.LogError("No valid location: " + _location);
            return new RectTransform();
        }
        RectTransform rHighlighDay = new RectTransform();
        foreach (Day day in days[_location])
        {
            Text txtTime = Instantiate(lessonPrefab, contenTransform);
            NormalDay normalDay = day as NormalDay;
            if (normalDay != null)
            {
                if (normalDay.Equals(_highlightDay))
                {
                    txtTime.text = Config.mainCultureInfo.DateTimeFormat.GetDayName(normalDay.date.DayOfWeek) + ", " + normalDay.date.Date.ToString("d", Config.mainCultureInfo);
                    txtTime.fontStyle = FontStyle.Bold;
                    txtTime.color = new Color(0.8f, 0.2f, 0.2f);
                    rHighlighDay = txtTime.gameObject.GetComponent<RectTransform>();
                }
                else
                {
                    txtTime.text = Config.mainCultureInfo.DateTimeFormat.GetDayName(normalDay.date.DayOfWeek) + ", " + normalDay.date.Date.ToString("d", Config.mainCultureInfo);
                    txtTime.fontStyle = FontStyle.Bold;
                }

                foreach (Lesson lesson in normalDay.lessons)
                {
                    Text txt = Instantiate(lessonPrefab, contenTransform);
                    txt.text = lesson.ToString();
                }
            }
            else if(day is InfoDay)
            {
                txtTime.text = ((InfoDay)day).info;
                txtTime.fontStyle = FontStyle.Bold;
                txtTime.fontStyle = FontStyle.BoldAndItalic;
            }
            else { //Should never happen
                throw new InvalidCastException("No such class: " + day.GetType().Name);
            }
        }
        return rHighlighDay;
    }

    private void centerScrollViewToItem(RectTransform _target)
    {
        Canvas.ForceUpdateCanvases();

        contenTransform.position = new Vector3(0f, Math.Abs(_target.localPosition.y), 0f);

        Debug.Log(contenTransform.position.y);

    }

    private void clearDisplayedData()
    {
        foreach (Transform t in contenTransform)
        {
            Destroy(t.gameObject);
        }
    }
}
