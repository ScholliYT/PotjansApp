using UnityEngine;

public class btnManager : MonoBehaviour {

    public void click(string _location) {
        Manager.instance.displayLocalData(_location);
    }
}
