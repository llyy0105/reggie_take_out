package com.ly.reggie.dto;

import com.ly.reggie.entity.Setmeal;
import com.ly.reggie.entity.SetmealDish;
import com.ly.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
