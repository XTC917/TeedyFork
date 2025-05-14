/**
 * Document detail controller.
 */
angular.module('docs').controller('DocumentDetailController', ['$scope', '$modal', 'DocumentService', 'NotificationService',
  function($scope, $modal, DocumentService, NotificationService) {
    // ... existing code ...

    /**
     * Open the translation modal.
     */
    $scope.openTranslateModal = function() {
      var modalInstance = $modal.open({
        templateUrl: 'partial/docs/document.translate.html',
        controller: 'DocumentModalTranslate',
        resolve: {
          document: function() {
            return $scope.document;
          }
        }
      });

      modalInstance.result.then(function() {
        // Refresh the document after translation
        loadDocument();
        NotificationService.success('document.translate.success');
      }, function() {
        // Modal dismissed
      });
    };

    /**
     * Load the document.
     */
    function loadDocument() {
      DocumentService.getDocument($scope.documentId)
        .then(function(response) {
          $scope.document = response.data;
        })
        .catch(function(error) {
          NotificationService.error('document.translate.error');
        });
    }

    // ... rest of the controller code ...
  }
]); 