package com.grigoriadi.tnt.homework.client;

import java.util.List;
import java.util.Map;

public interface TrackClient {
    Map<String, String> getTrack(List<String> trackIds);
}
