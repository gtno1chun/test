// 실행노드 지정
node("ubuntu-ssh") {
    // ##### 파이프라인 변수 정의 #####
    def GIT_URL="http://172.18.0.84/sample/petclinic.git"
    def GIT_BRANCH="develop"
    def GIT_CLONE_DIR=GIT_URL.find(/[^\/]*$/).replace(".git", "")
    def GIT_CREDENTIAL_ID="gitlab-cicdbot-token"

    def BUILD_ENV="${params.ENVIRONMENT}"
    def BUILD_TAG="${env.BUILD_NUMBER}"
    def BUILD_JDK="jdk17"
    def BUILD_MAVEN="maven3.9.6"
    def BUILD_MAVEN_SETTINGS_CONFIG_ID="maven-default-settings"

    def REGISTRY_PROTOCOL="http"
    def REGISTRY_SERVER="172.18.0.27"
    def REGISTRY_URL="${REGISTRY_PROTOCOL}://${REGISTRY_SERVER}"
    def REGISTRY_CREDENTIAL_ID="container-registry-harbor-credential"

    def K8S_NAMESPACE="sample-${BUILD_ENV}"
    def K8S_APP_NAME="petclinic"
    def K8S_APP_IMAGE="${REGISTRY_SERVER}/${K8S_NAMESPACE}/${K8S_APP_NAME}"
    def K8S_CREDENTIAL_ID="kubeconfig-default-cicdbot"

    // ##### 파이프라인 속성 정의 #####
    properties([
        disableConcurrentBuilds() // 동시 빌드 허용 안 함
        , buildDiscarder(logRotator(numToKeepStr: "10", artifactNumToKeepStr: "10")) // 오래된 빌드 삭제
        , parameters([
            gitParameter(
                name: "BRANCH"
                , type: "PT_BRANCH"
                , description: "Select The Branch."
                , quickFilterEnabled: false
                , branchFilter: "origin/(.*)"
                , defaultValue: GIT_BRANCH
                , selectedValue: "DEFAULT"
                , sortMode: "DESCENDING_SMART"
                , useRepository: GIT_URL
            )
            , choice(name: "ENVIRONMENT", choices: ["dev", "prd"], description: "Select The Environment", defaultValue: "dev")
        ]) // 빌드 파라메터
    ])

    // ##### 파이프라인 작업 정의 #####
    stage("Checkout") {
        currentBuild.description="BRANCH: ${params.BRANCH}, ENVIRONMENT: ${params.ENVIRONMENT}"
        dir(GIT_CLONE_DIR) {
            checkout([$class: "GitSCM"
                , branches: [[name: params.BRANCH]]
                , userRemoteConfigs: [[url: GIT_URL, credentialsId: GIT_CREDENTIAL_ID]]
                // , extensions: [[$class: "GitLFSPull"]]
            ])
        }
    }

    stage("Maven Build") {
        dir(GIT_CLONE_DIR) {
            withMaven(maven: BUILD_MAVEN, mavenSettingsConfig: BUILD_MAVEN_SETTINGS_CONFIG_ID, jdk: BUILD_JDK) {
                sh """
                    mvn clean package -B -U -DskipTests=true -Dmaven.test.skip=true -P${BUILD_ENV}
                """
            }
        }
    }

    stage("Docker Build") {
        dir(GIT_CLONE_DIR) {
            sh """
                docker build \
                    --build-arg REGISTRY_SERVER=${REGISTRY_SERVER} \
                    --build-arg BUILD_ENV=${BUILD_ENV} \
                    -f Dockerfile \
                    -t ${K8S_APP_IMAGE}:${BUILD_TAG} \
                    -t ${K8S_APP_IMAGE}:latest .
            """
        }
    }

    stage("Push Image") {
        docker.withRegistry(REGISTRY_URL, REGISTRY_CREDENTIAL_ID) {
            docker.image("${K8S_APP_IMAGE}:${BUILD_TAG}").push()
            docker.image("${K8S_APP_IMAGE}:latest").push()
        }
    }

    stage("Cleanup Image") {
        sh """
            docker rmi \
                ${K8S_APP_IMAGE}:${BUILD_TAG} \
                ${K8S_APP_IMAGE}:latest
        """
    }

    stage("Replace kubernetes.yaml") {
        dir(GIT_CLONE_DIR) {
            def filePath = "../${GIT_CLONE_DIR}-${BUILD_ENV}/kubernetes.yaml"
            writeFile(file: filePath, text: readFile("kubernetes.yaml"))
            sh """
                # Replace
                sed -i 's/\${V_K8S_APP_NAME}/${K8S_APP_NAME}/g' ${filePath}
                sed -i 's/\${V_K8S_APP_IMAGE}/${K8S_APP_IMAGE.replaceAll("/", "\\\\/")}/g' ${filePath}
                sed -i 's/\${V_BUILD_TAG}/${BUILD_TAG}/g' ${filePath}
                sed -i 's/\${V_BUILD_ENV}/${BUILD_ENV}/g' ${filePath}
            """
        }
    }

    stage("Apply kubernetes.yaml") {
        dir("${GIT_CLONE_DIR}-${BUILD_ENV}") {
            withCredentials([file(credentialsId: K8S_CREDENTIAL_ID, variable: "KUBECONFIG")]) {
                sh """
                    kubectl apply -f kubernetes.yaml \
                        -n ${K8S_NAMESPACE} --kubeconfig="""+'$KUBECONFIG'+"""
                """
                timeout(time: 1, unit: 'MINUTES') {
                    try {
                        sh """
                            kubectl rollout status deploy -l app=${K8S_APP_NAME} --watch \
                            -n ${K8S_NAMESPACE} --kubeconfig="""+'$KUBECONFIG'+"""
                        """
                    } catch(err) {
                        throw err
                    } finally {
                        sh """
                            kubectl describe deploy -l app=${K8S_APP_NAME} \
                            -n ${K8S_NAMESPACE} --kubeconfig="""+'$KUBECONFIG'+"""
                        """
                    }
                }
            }
        }
    }
}