//
//  QRCodeViewController.swift
//  HNTS
//
//  Created by Adrift on 2016/12/4.
//  Copyright © 2016年 华农天时. All rights reserved.
//

import UIKit
import AVFoundation

extension QRCodeViewController: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
        guard let image = info["UIImagePickerControllerOriginalImage"] as? UIImage else {
            return
        }
        guard let ciImage = CIImage(image: image) else {
            return
        }
        //创建探测器
        let detector =  CIDetector(ofType: CIDetectorTypeQRCode, context: nil, options: [CIDetectorAccuracy: CIDetectorAccuracyHigh])
        //利用探测器探测数据
        let results = detector?.features(in: ciImage)
        //取出探测到的数据
        guard let tmpResults = results else {
            return
        }
        for result in tmpResults {
            let feature = result as! CIQRCodeFeature
            if let value = feature.messageString {
                picker.dismiss(animated: true, completion: nil)
                dismiss(animated: false, completion: nil)
                closure?(value)
            }
        }
    }
}

class QRCodeViewController: UIViewController {
    
    var lightOn = false
    
    var closure:((String) -> ())? //添加农场成功之后的回调
    
    /// 扫描区域
    @IBOutlet weak var customContainerView: UIView!
    /// 容器高度约束
    @IBOutlet weak var containerHeightCons: NSLayoutConstraint!
    /// 冲击波顶部约束
    @IBOutlet weak var scanLineCons: NSLayoutConstraint!
    override func viewDidLoad() {
        super.viewDidLoad()
        navigationItem.title = "扫一扫"
        scanQRCode()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        UIApplication.shared.statusBarStyle = .lightContent
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        UIApplication.shared.statusBarStyle = .default
    }
    
    @IBAction func backClick(_ sender: Any) {
        dismiss(animated: true, completion: nil)
    }
    
    @IBAction func selectImageClick(_ sender: Any) {
        let imagePicker = UIImagePickerController()
        imagePicker.delegate = self
        present(imagePicker, animated: true, completion: nil)
    }
    
    @IBAction func flashLightClick(_ sender: Any) {
        if let d = self.device {
            do {
                try d.lockForConfiguration()
                if d.torchMode == .on {
                    d.torchMode = .off
                } else {
                    try d.setTorchModeOn(level: 1.0)
                }
                d.unlockForConfiguration()
            } catch {
                print(error)
            }
        }
    }
    override var preferredStatusBarStyle: UIStatusBarStyle {
        if #available(iOS 13.0, *) {
            return .lightContent
        } else {
            return .default
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        view.backgroundColor = UIColor.clear
        scanLineCons.constant = -containerHeightCons.constant
        view.layoutIfNeeded()
        
        UIView.animate(withDuration: 1.0, animations: {
            UIView.setAnimationRepeatCount(MAXFLOAT)
            self.scanLineCons.constant = self.containerHeightCons.constant
            self.view.layoutIfNeeded()
        })
    }
    
    lazy var device: AVCaptureDevice? = {
        let d = AVCaptureDevice.default(for: AVMediaType.video)
        return d
    }()
    
    //MARK: - 内部方法
    
    private func scanQRCode(){
        
        guard let input = input else {
            return
        }
        if !session.canAddInput(input) {
            return
        }
        if !session.canAddOutput(output) {
            return
        }
        session.addInput(input)
        session.addOutput(output)
        
        output.metadataObjectTypes = output.availableMetadataObjectTypes
        
        
        output.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
    
        view.layer.insertSublayer(previewLayer, at: 0)
        previewLayer.frame = view.bounds
        
        view.layer.addSublayer(containerLayer)
        containerLayer.frame = view.bounds
        
        session.startRunning()
    }
    
    //MARK: - 懒加载
    /// 输入对象
    private lazy var input: AVCaptureDeviceInput? = {
        let device = AVCaptureDevice.default(for: AVMediaType.video)
        guard let tmp = device else {
            return nil
        }
        return try? AVCaptureDeviceInput(device: tmp)
    }()
    
    /// 会话
    fileprivate lazy var session: AVCaptureSession = AVCaptureSession()
    
    /// 输出对象
    private lazy var output: AVCaptureMetadataOutput = {
        let output = AVCaptureMetadataOutput()
        let screenHeight = UIScreen.main.bounds.height
        let screenWidth = UIScreen.main.bounds.width
        let x = (customContainerView.y + customContainerView.height / 2) / self.view.height
        let y = (self.view.width - customContainerView.x - customContainerView.width) / self.view.width
        let width = customContainerView.height / self.view.height
        let height = customContainerView.width / self.view.width
        output.rectOfInterest = CGRect(x: x, y: y, width: width, height: height)
        
        return output
    }()
    
    /// 预览图层
    fileprivate lazy var previewLayer: AVCaptureVideoPreviewLayer = AVCaptureVideoPreviewLayer(session: self.session)

    /// 存放描边
    fileprivate lazy var containerLayer: CALayer = CALayer()
}

extension QRCodeViewController: AVCaptureMetadataOutputObjectsDelegate {

    /// 只要扫描到二维码,就会调用
    ///
    /// - Parameters:
    ///   - captureOutput: captureOutput
    ///   - metadataObjects: metadataObjects
    ///   - connection: connection
//    这个是swift3.2下的方法.🙂自己感受一下.
//    func captureOutput(_ captureOutput: AVCaptureOutput!, didOutputMetadataObjects metadataObjects: [Any]!, from connection: AVCaptureConnection!)
    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        let tmp = metadataObjects.last as? AVMetadataMachineReadableCodeObject
        clearLayers()
        if let tmp = tmp {
            let objc = previewLayer.transformedMetadataObject(for: tmp) as! AVMetadataMachineReadableCodeObject

            guard let value = objc.stringValue else {
                return
            }
            dismiss(animated: true, completion: nil)
            closure?(value)
        }
    }
    
    /// 描边
//    private func drawLines(objc: AVMetadataMachineReadableCodeObject) {
//
//        let arr = objc.corners
//
//        let layer = CAShapeLayer()
//        layer.lineWidth = 2
//        layer.strokeColor = colorTheme.cgColor
//        layer.fillColor = colorClear.cgColor
//
//        let path = UIBezierPath()
//
//        var point: CGPoint = CGPoint.zero
//
//        for (index, tmp) in objc.corners.enumerated() {
//            if index == 0 {
//                point = CGPoint(dictionaryRepresentation: (tmp as! CFDictionary))!
//                path.move(to: point)
//            } else {
//                point = CGPoint(dictionaryRepresentation: (tmp as! CFDictionary))!
//                path.addLine(to: point)
//            }
//        }
    
//        for index in objc.corners.indices {
//            if index == 0 {
//                point = CGPoint(dictionaryRepresentation: (arr[index] as! CFDictionary))!
//                path.move(to: point)
//            } else {
//                point = CGPoint(dictionaryRepresentation: (arr[index] as! CFDictionary))!
//                path.addLine(to: point)
//            }
//        }
//
//        path.close()
//
//        layer.path = path.cgPath
//
//        containerLayer.addSublayer(layer)
//    }
    
    /// 从container中删除已经描好的边
    private func clearLayers() {
        if let subLayers = containerLayer.sublayers {
            for layer in subLayers {
                layer.removeFromSuperlayer()
            }
        }
    }
}
