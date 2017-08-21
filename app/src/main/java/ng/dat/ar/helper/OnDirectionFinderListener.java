package ng.dat.ar.helper;

import java.util.List;

import ng.dat.ar.model.Direction;


/**
 * Created by Brucelee Thanh on 21/08/2017.
 */

public interface OnDirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Direction> directions);
}
