package com.tgy.rtls.data.entity.park;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Todo implements Serializable {
    private String todo;
    private String data;
    private String time;
}