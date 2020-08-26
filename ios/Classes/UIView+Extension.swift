//
//  UIView+Extension.swift
//  chaobaimiye
//
//  Created by 华农天时 on 2016/11/29.
//  Copyright © 2016年 Jun Dang. All rights reserved.
//

import Foundation

extension UIView {
    
    /// X坐标
    var x: CGFloat {
        set {
            frame.origin.x = newValue
        }
        
        get {
            return frame.origin.x
        }
    }
    
    /// Y坐标
    var y: CGFloat {
        set {
            frame.origin.y = newValue
        }
        get {
            return frame.origin.y
        }
    }
    
    /// 宽度
    var width: CGFloat {
        set {
            frame.size.width = newValue
        }
        get {
            return frame.width
        }
    }
    
    /// 高度
    var height: CGFloat {
        set {
            frame.size.height = newValue
        }
        
        get {
            return frame.size.height
        }
    }
    
    /// X,Y坐标点
    var origin: CGPoint {
        set {
            frame.origin = newValue
        }
        get {
            return frame.origin
        }
    }
    
    /// 大小
    var size: CGSize {
        set {
            frame.size = newValue
        }
        get {
            return frame.size
        }
    }
    
    /// 上
    var top: CGFloat {
        set {
            frame.origin.y = newValue
        }
        get {
            return frame.origin.y
        }
    }
    
    var topAdd: CGFloat {
        set {
            frame.origin.y += newValue
        }
        get {
            return frame.origin.y
        }
    }
    
    /// 左
    var left: CGFloat {
        set {
            frame.origin.x = newValue
        }
        get {
            return frame.origin.x
        }
    }
    
    var leftAdd: CGFloat {
        set {
            frame.origin.x += newValue
        }
        get {
            return frame.origin.x
        }
    }
    
    /// 下
    var bottom: CGFloat {
        set {
            frame.origin.y = newValue - frame.size.height
        }
        get {
            return frame.origin.y + frame.size.height
        }
    }
}
