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
    
    /// 是否显示我的二维码
    var isShowSelf = false
    
    /**
     @brief  闪关灯开启状态
     */
    var isOpenedFlash: Bool = false
    
    //MARK: - 底部几个功能：开启闪光灯、相册、我的二维码
    
    /// 底部显示的功能项
    var bottomItemsView = UIView()
    
    /// 标题
    let titleLabel = UILabel()
    
    /// 相册
    let btnPhoto = UIButton()
    
    /// 闪光灯
    let btnFlash = UIButton()
    
    /// 闪光灯文字
    let labelFlash = UILabel()
    
    /// 我的二维码
    let myQRButton = UIButton()
    
    /// 返回按钮
    let backButton = UIButton()
    
    /// 提示文字
    let promptLabel = UILabel()
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        UIApplication.shared.statusBarStyle = .lightContent
        
        var style = LBXScanViewStyle()
        style.centerUpOffset = 100
        style.photoframeAngleStyle = LBXScanViewPhotoframeAngleStyle.Inner
        style.photoframeLineW = 4
        style.photoframeAngleW = 23
        style.photoframeAngleH = 23
        style.isNeedShowRetangle = false
        style.anmiationStyle = LBXScanViewAnimationStyle.LineMove
        style.colorAngle = UIColor(red: 254/255, green: 107/255.0, blue: 6/255.0, alpha: 1.0)
        style.animationImage = UIImage(named: "qrcode_Scan_weixin_Line", in: bundle, compatibleWith: nil)
        scanStyle = style
        //需要识别后的图像
        setNeedCodeImage(needCodeImg: true)
        
        //框向上移动10个像素
        scanStyle?.centerUpOffset += 10
        
        scanResultDelegate = self
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        drawBackButton()
        drawLabels()
        drawBottomItems()
    }
    
    func drawBackButton() {
        backButton.setImage(UIImage(named: "back", in: bundle, compatibleWith: nil), for: .normal)
        backButton.frame = CGRect(x: 20, y: 40, width: 25, height: 50)
        backButton.addTarget(self, action: #selector(backButtonClick), for: .touchUpInside)
        view.addSubview(backButton)
    }
    
    func drawLabels() {
        
        titleLabel.text = "扫一扫"
        titleLabel.textColor = UIColor.white
        if #available(iOS 8.2, *) {
            titleLabel.font = UIFont.systemFont(ofSize: 16, weight: .medium)
        } else {
            titleLabel.font = UIFont.systemFont(ofSize: 16)
        }
        titleLabel.textAlignment = .center
        titleLabel.size = CGSize(width: 100, height: 20)
        titleLabel.center = CGPoint(x: UIScreen.main.bounds.width / 2, y: 65)
        view.addSubview(titleLabel)
        
        promptLabel.text = "将二维码放入框内，即可自动扫描"
        promptLabel.textAlignment = .center
        promptLabel.textColor = UIColor.white
        promptLabel.font = UIFont.systemFont(ofSize: 14)
        promptLabel.size = CGSize(width: UIScreen.main.bounds.size.width, height: 20)
        let xScanRetangleOffset = scanStyle?.xScanRetangleOffset ?? 0
        let centerUpOffset = scanStyle?.centerUpOffset ?? 0
        let offset = view.frame.width - xScanRetangleOffset * 2
        promptLabel.center = CGPoint(x: view.frame.width/2, y: (view.frame.height - centerUpOffset + offset) / 2 - 20)
        view.addSubview(promptLabel)
    }
    
    func drawBottomItems() {
        let xScanRetangleOffset = scanStyle?.xScanRetangleOffset ?? 0
        let centerUpOffset = scanStyle?.centerUpOffset ?? 0
        let offset = view.frame.width - xScanRetangleOffset * 2
        bottomItemsView.size = CGSize(width: view.frame.size.width, height: 100)
        bottomItemsView.center = CGPoint(x: view.frame.size.width / 2, y: (view.frame.height - centerUpOffset + offset) / 2 + 100)
        self.view.addSubview(bottomItemsView)
        
        
        labelFlash.text = "轻点照亮"
        labelFlash.textColor = UIColor.white
        labelFlash.font = UIFont.systemFont(ofSize: 14)
        labelFlash.textAlignment = NSTextAlignment.center
        labelFlash.size = CGSize(width: UIScreen.main.bounds.width, height: 20)
        labelFlash.center = CGPoint(x: bottomItemsView.frame.width/2, y: bottomItemsView.frame.height/2 + 15)
        
        let size = CGSize(width: 44, height: 44)
        btnFlash.bounds = CGRect(x: 0, y: 0, width: size.width, height: size.height)
//        btnFlash.center = CGPoint(x: bottomItemsView!.frame.width * 2 / 7, y: bottomItemsView!.frame.height/2)
        btnFlash.center = CGPoint(x: bottomItemsView.frame.width/2, y: bottomItemsView.frame.height/2 - 15)
        btnFlash.setImage(UIImage(named: "qrcode_scan_btn_flash_nor", in: bundle, compatibleWith: nil), for:UIControl.State.normal)
        btnFlash.addTarget(self, action: #selector(openOrCloseFlash), for: UIControl.Event.touchUpInside)
        
        
        btnPhoto.bounds = btnFlash.bounds
        btnPhoto.backgroundColor = UIColor(red: 102/255, green: 102/255.0, blue: 102/255.0, alpha: 0.8)
        btnPhoto.layer.cornerRadius = 22
//        btnPhoto.center = CGPoint(x: bottomItemsView!.frame.width * 5/7, y: bottomItemsView!.frame.height/2)
        btnPhoto.center = CGPoint(x: bottomItemsView.frame.width * 3/4 + 40, y: bottomItemsView.frame.height/2)
        btnPhoto.setImage(UIImage(named: "qrcode_scan_btn_photo", in: bundle, compatibleWith: nil), for: UIControl.State.normal)
        btnPhoto.addTarget(self, action: #selector(openPhotoAlbum), for: UIControl.Event.touchUpInside)
                
        myQRButton.bounds = btnFlash.bounds;
        myQRButton.isHidden = !isShowSelf
        myQRButton.backgroundColor = UIColor(red: 102/255, green: 102/255.0, blue: 102/255.0, alpha: 0.8)
        myQRButton.layer.cornerRadius = 22
        myQRButton.center = CGPoint(x: bottomItemsView.frame.width / 4 - 40, y: bottomItemsView.frame.height/2);
        myQRButton.setImage(UIImage(named: "qrcode_scan_btn_myqrcode", in: bundle, compatibleWith: nil), for: UIControl.State.normal)
        myQRButton.addTarget(self, action: #selector(myCode), for: UIControl.Event.touchUpInside)
        
        bottomItemsView.addSubview(labelFlash)
        bottomItemsView.addSubview(btnFlash)
        bottomItemsView.addSubview(btnPhoto)
        bottomItemsView.addSubview(myQRButton)
        
        view.addSubview(bottomItemsView)
    }
    
    /// 开关闪光灯
    @objc func openOrCloseFlash() {
        scanObj?.changeTorch()
        
        isOpenedFlash = !isOpenedFlash
        
        if isOpenedFlash {
            btnFlash.setImage(UIImage(named: "qrcode_scan_btn_flash_down", in: bundle, compatibleWith: nil), for:UIControl.State.normal)
            labelFlash.text = "轻点关闭"
        } else {
            btnFlash.setImage(UIImage(named: "qrcode_scan_btn_flash_nor", in: bundle, compatibleWith: nil), for:UIControl.State.normal)
            labelFlash.text = "轻点照亮"
        }
    }
    
    /// 我的二维码
    @objc func myCode() {
        closure?("MY_QR_CODE")
        dismiss(animated: false, completion: nil)
    }
    
    /// 返回按钮
    @objc func backButtonClick() {
        closure?("")
        dismiss(animated: true, completion: nil)
    }
    
    lazy var bundle: Bundle = {
        guard let url = Bundle(for: Self.self).url(forResource: "qrscan", withExtension: "bundle"), let bundle = Bundle(url: url) else {
            return Bundle.main
        }
        return bundle
    }()
}

extension ScanViewController: LBXScanViewControllerDelegate {
    func scanFinished(scanResult: LBXScanResult, error: String?) {
        dismiss(animated: true, completion: nil)
        if let _ = error {
            closure?("")
        } else {
            closure?(scanResult.strScanned ?? "")
        }
    }
}
