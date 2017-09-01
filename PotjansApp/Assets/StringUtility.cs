using System;

public static class StringUtility {
    public static string getBetween(string _strSource, string _strStart, string _strEnd)
    {
        if (!_strSource.Contains(_strStart) || !_strSource.Contains(_strEnd)) return "";
        int start = _strSource.IndexOf(_strStart, 0, StringComparison.Ordinal) + _strStart.Length;
        int end = _strSource.IndexOf(_strEnd, start, StringComparison.Ordinal);
        return _strSource.Substring(start, end - start);
    }
}
