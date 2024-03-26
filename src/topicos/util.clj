(ns topicos.util
  (:import java.util.Base64))

(defn encode-base64
  "Encodes data into a base64 string"
  [data]
  (.encodeToString (Base64/getUrlEncoder) (.getBytes data)))

(defn decode-base64
  "Decodes data from a base64 string"
  [data]
  (String. (.decode (Base64/getUrlDecoder) (.getBytes data))))
