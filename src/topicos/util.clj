(ns topicos.util
  (:import org.apache.commons.codec.binary.Base64))

(defn slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [x]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (-> x
        clojure.java.io/input-stream
        (clojure.java.io/copy out))
    (.toByteArray out)))

(defn encode-base64
  "Encodes byte-array into a base64 string"
  [data]
  (Base64/encodeBase64URLSafeString data))


(defn decode-base64
  "Decodes data from a base64 string"
  [data]
  (Base64/decodeBase64 data))

