(ns lineup.util
  "Utility functions. TODO potentially move these into a common library later.")

(defn remove-nil-keys
  "Remove nil keys from a map"
  [m]
  (apply dissoc m
         (for [[k v] m :when (nil? v)] k)))