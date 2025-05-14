angular.module('docs').controller('DocumentViewController', [
    '$scope',
    '$stateParams',
    '$state',
    '$uibModal',
    '$translate',
    'DocumentService',
    'NotificationService',
    function($scope, $stateParams, $state, $uibModal, $translate, DocumentService, NotificationService) {
        $scope.document = null;
        $scope.files = [];
        $scope.openedFile = null;
        $scope.displayMode = 'grid';

        // Load document
        $scope.loadDocument = function() {
            DocumentService.getDocument($stateParams.id)
                .then(function(response) {
                    $scope.document = response.data;
                    $scope.files = response.data.files || [];
                });
        };

        // Open file
        $scope.openFile = function(file, event) {
            if (event) {
                event.preventDefault();
            }
            $scope.openedFile = file;
            $state.go('document.view.file', { fileId: file.id });
        };

        // Rename file
        $scope.renameFile = function(file) {
            var modalInstance = $uibModal.open({
                templateUrl: 'partial/docs/document.file.rename.html',
                controller: 'DocumentModalFileRename',
                resolve: {
                    file: function() {
                        return file;
                    }
                }
            });

            modalInstance.result.then(function() {
                $scope.loadDocument();
            });
        };

        // Process file
        $scope.processFile = function(file) {
            DocumentService.processFile(file.id)
                .then(function() {
                    $scope.loadDocument();
                    NotificationService.success($translate.instant('document.view.content.file_processing_started'));
                });
        };

        // Translate file
        $scope.translateFile = function(file) {
            console.log('translateFile called', { document: $scope.document, file: file });
            
            if (!$scope.document || !file) {
                console.error('Missing document or file information');
                return;
            }
            
            var modalInstance = $uibModal.open({
                templateUrl: 'partial/docs/document.translate.html',
                controller: 'DocumentModalTranslate',
                resolve: {
                    document: function() {
                        return $scope.document;
                    },
                    file: function() {
                        return file;
                    }
                }
            });

            modalInstance.result.then(function() {
                $scope.loadDocument();
                NotificationService.success($translate.instant('document.translate.success'));
            }).catch(function(error) {
                console.error('Translation modal error', error);
            });
        };

        // Delete file
        $scope.deleteFile = function(file) {
            var modalInstance = $uibModal.open({
                templateUrl: 'partial/docs/document.file.delete.html',
                controller: 'DocumentModalFileDelete',
                resolve: {
                    file: function() {
                        return file;
                    }
                }
            });

            modalInstance.result.then(function() {
                $scope.loadDocument();
            });
        };

        // Upload new version
        $scope.uploadNewVersion = function(files, file) {
            DocumentService.uploadNewVersion(files[0], file.id)
                .then(function() {
                    $scope.loadDocument();
                });
        };

        // Open versions
        $scope.openVersions = function(file) {
            var modalInstance = $uibModal.open({
                templateUrl: 'partial/docs/document.file.versions.html',
                controller: 'DocumentModalFileVersions',
                resolve: {
                    file: function() {
                        return file;
                    }
                }
            });
        };

        // File dropped
        $scope.fileDropped = function(files, event) {
            if (event) {
                event.preventDefault();
            }
            DocumentService.uploadFiles(files, $stateParams.id)
                .then(function() {
                    $scope.loadDocument();
                });
        };

        // Initialize
        $scope.loadDocument();
    }
]); 