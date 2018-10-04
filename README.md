# Nexus scripts

## 1. Development and deployment
- Script is posted at nexus task manager ([repository.k8s.strix.kr](https://repository.k8s.strix.kr/#admin/system/tasks:98aa5de7-09dd-4584-85a3-31e733125871))
- Run script at nexus and see the logs by `watch -- kubectl -n default log -l app=sonatype-nexus -c nexus --tail=20`

## 2. References
[Nexus official document](https://help.sonatype.com/repomanager3) gives no help but here is a helpful references for scripting: [Nexus book examples](https://github.com/sonatype/nexus-book-examples/tree/nexus-3.x/scripting/nexus-script-example/src/main/groovy)

## 3. Scripts
### cleanup-jenkins-docker-images.groovy
- Find nexus components from docker-hosted repos which are versioned with `jenkins-` prefixed and at least one hour passed after updated.
- Delete them
