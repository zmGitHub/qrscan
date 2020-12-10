//
//  QQScanViewController.swift
//  swiftScan
//
//  Created by xialibing on 15/12/10.
//  Copyright © 2015年 xialibing. All rights reserved.
//

import UIKit

class ScanViewController: LBXScanViewController {
    
    var closure :((String) -> ())? //扫码成功回调
    /**
     @brief  扫码区域上方提示文字
     */
    var topTitle: UILabel?
    
    /**
     @brief  闪关灯开启状态
     */
    var isOpenedFlash: Bool = false
    
    //MARK: - 底部几个功能：开启闪光灯、相册、我的二维码
    
    // 底部显示的功能项
    var bottomItemsView: UIView?
    
    // 相册
    var btnPhoto: UIButton = UIButton()
    
    // 闪光灯
    var btnFlash: UIButton = UIButton()
    
    // 返回按钮
    var backButton = UIButton()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //需要识别后的图像
        setNeedCodeImage(needCodeImg: true)
        
        //框向上移动10个像素
        scanStyle?.centerUpOffset += 10
        
        // Do any additional setup after loading the view.
    }
    
    override func viewDidAppear(_ animated: Bool) {
        
        super.viewDidAppear(animated)
        
        drawBottomItems()
        drawBackButton()
    }
    
    override func handleCodeResult(arrayResult: [LBXScanResult]) {
        dismiss(animated: true, completion: nil)
        let result = arrayResult.first?.strScanned ?? ""
        closure?(result)
    }
    
    func drawBackButton() {
        backButton.setImage(UIImage(named: "back", in: bundle, compatibleWith: nil), for: .normal)
        backButton.frame = CGRect(x: 20, y: 40, width: 25, height: 50)
        backButton.addTarget(self, action: #selector(backButtonClick), for: .touchUpInside)
        view.addSubview(backButton)
    }
    
    @objc func backButtonClick() {
        dismiss(animated: true, completion: nil)
    }
    
    func drawBottomItems() {
        if (bottomItemsView != nil) {
            return
        }
        
        let yMax = self.view.frame.maxY - self.view.frame.minY
        
        bottomItemsView = UIView(frame: CGRect(x: 0.0, y: yMax-100, width: self.view.frame.size.width, height: 100 ) )
        
        bottomItemsView!.backgroundColor = UIColor(red: 0.0, green: 0.0, blue: 0.0, alpha: 0.6)
        
        self.view.addSubview(bottomItemsView!)
        
        let size = CGSize(width: 65, height: 87)
        
        self.btnFlash = UIButton()
        btnFlash.bounds = CGRect(x: 0, y: 0, width: size.width, height: size.height)
        btnFlash.center = CGPoint(x: bottomItemsView!.frame.width * 2 / 7, y: bottomItemsView!.frame.height/2)
        
        btnFlash.setImage(UIImage(named: "qrcode_scan_btn_flash_nor", in: bundle, compatibleWith: nil), for:UIControl.State.normal)
        btnFlash.addTarget(self, action: #selector(ScanViewController.openOrCloseFlash), for: UIControl.Event.touchUpInside)
        
        
        self.btnPhoto = UIButton()
        btnPhoto.bounds = btnFlash.bounds
        btnPhoto.center = CGPoint(x: bottomItemsView!.frame.width * 5/7, y: bottomItemsView!.frame.height/2)
        btnPhoto.setImage(UIImage(named: "qrcode_scan_btn_photo_nor", in: bundle, compatibleWith: nil), for: UIControl.State.normal)
        btnPhoto.setImage(UIImage(named: "qrcode_scan_btn_photo_down", in: bundle, compatibleWith: nil), for: UIControl.State.highlighted)
        
        btnPhoto.addTarget(self, action: #selector(openPhotoAlbum), for: UIControl.Event.touchUpInside)
        
        
        bottomItemsView?.addSubview(btnFlash)
        bottomItemsView?.addSubview(btnPhoto)
        
        view.addSubview(bottomItemsView!)
    }
    
    //开关闪光灯
    @objc func openOrCloseFlash() {
        scanObj?.changeTorch()
        
        isOpenedFlash = !isOpenedFlash
        
        if isOpenedFlash {
            btnFlash.setImage(UIImage(named: "qrcode_scan_btn_flash_down", in: bundle, compatibleWith: nil), for:UIControl.State.normal)
        } else {
            btnFlash.setImage(UIImage(named: "qrcode_scan_btn_flash_nor", in: bundle, compatibleWith: nil), for:UIControl.State.normal)
        }
    }
    
    lazy var bundle: Bundle = {
        guard let url = Bundle(for: Self.self).url(forResource: "qrscan", withExtension: "bundle"), let bundle = Bundle(url: url) else {
            return Bundle.main
        }
        return bundle
    }()
}
