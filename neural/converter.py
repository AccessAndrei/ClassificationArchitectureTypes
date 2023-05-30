 # установка TensorFlow 2.4.1
import tensorflow as tf
from tensorflow.keras.models import load_model

# загружаем модель из файла .h5
model = load_model('/Users/moneygrind/PycharmProjects/ML_forAksen/venv/neural/112>111.h5')

# создаем конвертер модели .h5 в .tflite
converter = tf.lite.TFLiteConverter.from_keras_model(model)

# выполняем конвертацию
tflite_model = converter.convert()

# записываем модель в файл формата .tflite
with open('/Users/moneygrind/PycharmProjects/ML_forAksen/venv/model.tflite', 'wb') as f:
    f.write(tflite_model)
