# FPGA Virtual Board K8s 部署

## 部署步骤

### 1. 开发环境部署
``` bash
kubectl apply -k overlays/dev
```

### 2. 生产环境部署
``` bash
kubectl apply -k overlays/prod
```

### 3. 访问服务
``` bash
    MySQL: ClusterIP mysql:3306

    Redis: ClusterIP redis:6379

    Nacos: NodePort 30048

    hdu-vboard: NodePort 30099
```
