using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class btnManager : MonoBehaviour {

    public void click(string location) {
        DownloadTable.instance.loadNewData(location);
    }
}
