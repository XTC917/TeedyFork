/**
 * Document translation modal controller.
 */
angular.module('docs').controller('DocumentModalTranslate', [
    '$scope',
    '$uibModalInstance',
    'document',
    'file',
    'DocumentService',
    'NotificationService',
    '$translate',
    function($scope, $uibModalInstance, document, file, DocumentService, NotificationService, $translate) {
        console.log('DocumentModalTranslate initialized', { document, file });
        
        $scope.document = document;
        $scope.file = file;
        $scope.translate = {
            targetLanguage: 'zh',
            includeMetadata: true
        };
        $scope.isGenerating = false;

        /**
         * Generate translated PDF.
         */
        $scope.translateDocument = function() {
            console.log('translateDocument called', {
                documentId: document.id,
                targetLanguage: $scope.translate.targetLanguage,
                includeMetadata: $scope.translate.includeMetadata
            });
            
            $scope.isGenerating = true;
            
            DocumentService.generateTranslatedPdf(
                document.id,
                $scope.translate.targetLanguage,
                $scope.translate.includeMetadata
            ).then(function(response) {
                console.log('PDF generation successful', response);
                // Create blob URL and trigger download
                var blob = new Blob([response.data], { type: 'application/pdf' });
                var url = window.URL.createObjectURL(blob);
                var link = document.createElement('a');
                link.href = url;
                link.download = document.title + '_translated.pdf';
                link.click();
                window.URL.revokeObjectURL(url);
                
                NotificationService.success($translate.instant('document.translate.success'));
                $uibModalInstance.close();
            }).catch(function(error) {
                console.error('PDF generation failed', error);
                NotificationService.error($translate.instant('document.translate.error'));
            }).finally(function() {
                $scope.isGenerating = false;
            });
        };

        /**
         * Cancel the modal.
         */
        $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };
    }
]); 