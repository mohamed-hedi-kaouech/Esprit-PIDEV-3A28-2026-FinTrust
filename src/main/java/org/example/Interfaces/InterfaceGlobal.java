package org.example.Interfaces;

import java.util.List;

public interface InterfaceGlobal<T> {
    boolean Add(T t);
    boolean Delete(Integer id);
    void Update(T t);
    List<T> ReadAll();
    T ReadId(Integer id);

}
