참조: 
https://tekton.dev/docs/getting-started/tasks/
https://nangman14.tistory.com/85



# Tekton 
1. install 
'''
$ kubectl apply --filename \
https://storage.googleapis.com/tekton-releases/pipeline/latest/release.yaml
'''

2. install tkn CLI
'''
$ brew install tektoncd-cli
'''

3. SSH 개인 키 파일이 ~/.ssh/id_rsa에 있다고 가정
'''
$ kubectl create secret generic ssh-key-secret --from-file=ssh-key=~/.ssh/id_rsa
'''
