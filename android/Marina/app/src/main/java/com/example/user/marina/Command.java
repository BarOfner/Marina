package com.example.user.marina;

import java.util.ArrayList;
import java.util.Map;

public interface Command{
    String execute( Map<String, Entity> entityList);
}