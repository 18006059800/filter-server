/**
 * Autogenerated by Thrift Compiler (0.9.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.zyx.filter.thrift;


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

/**
 * 关键字过滤级别
 */
public enum TFilterLevel implements org.apache.thrift.TEnum {
  ADJUST(1),
  /**
   * 关键字出现，就根据TBehaviorType做相应的调整（下面更高的过滤级别也做调整）
   */
  NOTICE(2),
  /**
   * 关键字出现，就标记可疑，但不限制发表
   */
  CHECK(3),
  /**
   * 关键字出现，就标记为待审核
   */
  FORBIDDEN(4);

  private final int value;

  private TFilterLevel(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static TFilterLevel findByValue(int value) { 
    switch (value) {
      case 1:
        return ADJUST;
      case 2:
        return NOTICE;
      case 3:
        return CHECK;
      case 4:
        return FORBIDDEN;
      default:
        return null;
    }
  }
}
