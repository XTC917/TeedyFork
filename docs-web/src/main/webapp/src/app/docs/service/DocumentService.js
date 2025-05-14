/**
 * Document service.
 */
angular.module('docs').service('DocumentService', ['$http', 'ConfigService',
  function($http, ConfigService) {
    /**
     * Translate a document.
     *
     * @param {string} documentId Document ID
     * @param {string} targetLanguage Target language code
     * @param {boolean} includeMetadata Whether to include metadata in translation
     * @returns {Promise} Promise
     */
    this.translateDocument = function(documentId, targetLanguage, includeMetadata) {
      return $http.post('/api/document/' + documentId + '/translate', {
        target_language: targetLanguage,
        include_metadata: includeMetadata
      });
    };

    /**
     * Generate a translated PDF of the document.
     *
     * @param {string} documentId Document ID
     * @param {string} targetLanguage Target language code
     * @param {boolean} includeMetadata Whether to include metadata in translation
     * @returns {Promise} Promise
     */
    this.generateTranslatedPdf = function(documentId, targetLanguage, includeMetadata) {
      console.log('Generating translated PDF', { documentId, targetLanguage, includeMetadata });
      
      var url = ConfigService.getBaseUrl() + '/api/document/' + documentId + '/pdf';
      var params = {
        translate: true,
        target_language: targetLanguage,
        include_metadata: includeMetadata
      };
      
      return $http.get(url, {
        params: params,
        responseType: 'blob',
        headers: {
          'Accept': 'application/pdf'
        }
      });
    };
  }
]); 